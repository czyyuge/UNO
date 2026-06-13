a# 更新日志

## 2026-06-13 — 抢出（Quick Play）功能

### 功能概述

新增"抢出"玩法：当任意玩家打出一张牌后，其他玩家如果手中有**完全相同**的牌（同颜色 + 同类型/同数字），可以立即抢出，插队到该玩家之前行动。回合从抢出者继续。

此规则为 UNO 常见 House Rule，增加游戏互动性和策略深度。

---

### 规则细节

| 条件     | 说明                                                         |
| -------- | ------------------------------------------------------------ |
| 触发时机 | 任意玩家出牌后、下一位玩家行动前                             |
| 抢出条件 | 手牌与顶牌完全相同（`card.equals(topCard)`）                 |
| 万能牌   | 不可用于抢出（Wild / Wild +4）                               |
| 罚牌期间 | 有 +2/+4 待处理时不可抢出（被罚者必须先抓牌）                |
| 连锁抢出 | 支持 — 抢出后顶牌不变，下一位玩家可继续抢出相同牌            |
| UNO 惩罚 | 抢出前会先检查上家是否忘喊 UNO                               |
| 回合归属 | 抢出后 `currentPlayerIndex` 跳至抢出者，回合从抢出者下家继续 |

---

### 修改文件

| 文件                                    | 修改内容                                                                       |
| --------------------------------------- | ------------------------------------------------------------------------------ |
| `src/com/uno/model/Message.java`        | 新增 `QUICK_PLAY` 消息类型常量                                                 |
| `src/com/uno/util/MessageUtils.java`    | 新增 `createQuickPlayMessage()` 工厂方法                                       |
| `src/com/uno/game/GameLogic.java`       | 新增 `canQuickPlay()` 判断方法 + `quickPlayCard()` 执行方法                    |
| `src/com/uno/server/RoomManager.java`   | `handleGameAction()` 新增 `QUICK_PLAY` case                                    |
| `src/com/uno/server/ClientHandler.java` | 消息路由中新增 `QUICK_PLAY`                                                    |
| `src/com/uno/ui/GameFrame.java`         | `createCardButton()` 中非自己回合时启用与顶牌完全相同的牌，点击发 `QUICK_PLAY` |

---

### GameLogic 新增代码

```java
public boolean canQuickPlay(Player player, Card card) {
    if (gameOver) return false;                          // 1. 游戏未结束
    if (getCurrentPlayer().equals(player)) return false; // 2. 不是当前回合玩家
    if (pendingDraw && cardsToDraw > 0) return false;    // 3. 无待处理罚牌
    if (GameConstants.isWildCard(card.getType())) return false; // 4. 非万能牌
    Card topCard = deck.getTopCard();
    if (!card.equals(topCard)) return false;             // 5. 与顶牌完全相同
    if (!player.getHand().contains(card)) return false;  // 6. 牌在手牌中
    return true;
}

public boolean quickPlayCard(Player player, Card card, String chosenColor) {
    if (!canQuickPlay(player, card)) return false;
    player.removeCard(card);
    deck.discard(card);
    if (player.hasWon()) {
        gameOver = true;
        winner = player.getName();
        return true;
    }
    currentPlayerIndex = players.indexOf(player);  // 回合跳至抢出者
    hasDrawnThisTurn = false;
    applyCardEffect(card);                         // 执行功能牌效果
    if (!player.isSaidUno() && player.getCardCount() == 1) {
        unoOffender = player.getName();            // UNO 标记
    }
    return true;
}
```

**复用已有能力：**

- `Card.equals()` — 判断两张牌是否完全相同（已有，直接复用）
- `applyCardEffect()` — 处理 SKIP / REVERSE / +2 等效果（已有，直接调用）

---

### 服务端处理流程

```
收到 QUICK_PLAY 消息
  → checkAndApplyUnoPenalty()    检查上家是否忘喊 UNO
  → logic.quickPlayCard()        执行抢出（含全部校验）
  → 如果游戏结束 → 广播 GAME_OVER
  → 否则 → broadcastGameState() 广播最新状态
```

### 客户端 UI 行为

```
updateUI() 刷新手牌
  → 遍历手牌，对每张牌调用 createCardButton(card, myTurn)
  → 非自己回合 + 牌与顶牌完全相同 → 按钮启用
  → 点击该按钮 → 发送 QUICK_PLAY 消息（而非 PLAY_CARD）
```

---

### 边界情况处理

| 场景                      | 行为                                             |
| ------------------------- | ------------------------------------------------ |
| 当前玩家尝试抢出          | `canQuickPlay` 返回 false（自己回合用正常出牌）  |
| 万能牌试图抢出            | `canQuickPlay` 返回 false（万能牌不参与抢出）    |
| 有 +2/+4 待处理时抢出     | `canQuickPlay` 返回 false（必须先抓罚牌）        |
| 多人同时抢出              | 服务端先到先处理；后到者因顶牌可能已变而被拒绝   |
| 抢出后剩 1 张且未喊 UNO   | `unoOffender` 标记抢出者，下家行动时触发检查     |
| 抢出功能牌（SKIP/REV/+2） | `applyCardEffect` 正常生效，效果作用于抢出者下家 |
| 连锁抢出                  | 抢出同名牌后顶牌不变，后续玩家可继续抢出         |
