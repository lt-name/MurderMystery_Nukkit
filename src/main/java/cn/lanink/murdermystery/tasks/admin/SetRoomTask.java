package cn.lanink.murdermystery.tasks.admin;

import cn.lanink.gamecore.utils.Language;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntityText;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author lt_name
 */
public class SetRoomTask extends PluginTask<MurderMystery> {

    private int setRoomSchedule = 9;
    private int backRoomSchedule = 10;
    private int nextRoomSchedule = 20;
    private boolean autoNext = false;

    private final Player player;
    private final Level level;
    private final Map<Integer, Item> playerInventory;
    private final Item offHandItem;
    private final Language language;

    private EntityText waitSpawnText;
    private final HashMap<String, EntityText> randomSpawnTexts = new HashMap<>();
    private final HashMap<String, EntityItem> goldSpawnTexts = new HashMap<>();

    private int particleEffectTick = 0;

    public SetRoomTask(Player player, Level level) {
        super(MurderMystery.getInstance());
        this.player = player;
        this.level = level;
        this.playerInventory = player.getInventory().getContents();
        this.offHandItem = player.getOffhandInventory().getItem(0);
        player.getInventory().clearAll();
        player.getUIInventory().clearAll();
        this.language = this.owner.getLanguage(player);
    }

    @Override
    public void onRun(int i) {
        if (!this.player.isOnline() ||
                this.player.getLevel() != this.level ||
                !this.owner.setRoomTask.containsKey(this.player)) {
            this.cancel();
            return;
        }
        Item item;
        if (this.setRoomSchedule > 9) {
            item = Item.get(340);
            item.setNamedTag(new CompoundTag()
                    .putInt("MurderMysteryItemType", 110));
            item.setCustomName(this.language.translateString("admin_setRoom_back"));
            this.player.getInventory().setItem(0, item);
        }else {
            this.player.getInventory().clear(0);
        }
        boolean canNext = false;
        Config config = this.owner.getRoomConfig(player.getLevel());
        switch (this.setRoomSchedule) {
            case 9: //设置房间显示名称
                this.backRoomSchedule = 9;
                this.nextRoomSchedule = 10;

                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setRoomName"));

                item = Item.get(347);//钟表
                item.setNamedTag(new CompoundTag()
                        .putInt("MurderMysteryItemType", 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setRoomName"));
                this.player.getInventory().setItem(4, item);

                if (!"".equals(config.getString("roomName", "").trim())) {
                    if (autoNext) {
                        this.setRoomSchedule(this.nextRoomSchedule);
                    }else {
                        canNext = true;
                    }
                }
                break;
            case 10: //设置游戏模式
                this.backRoomSchedule = 9;
                this.nextRoomSchedule = 20;

                this.player.sendTip(this.owner.getLanguage().translateString("admin_setRoom_setGameMode"));

                item = Item.get(347);//钟表
                item.setNamedTag(new CompoundTag()
                        .putInt("MurderMysteryItemType", 113));
                item.setCustomName(this.owner.getLanguage().translateString("admin_setRoom_setGameMode"));
                this.player.getInventory().setItem(4, item);

                String setMode = config.getString("gameMode", "").trim();
                if (!"".equals(setMode)) {
                    if (autoNext) {
                        this.setRoomSchedule(this.nextRoomSchedule);
                    }else {
                        canNext = true;
                    }
                }
                break;
            case 20: //设置等待出生点
                this.backRoomSchedule = 10;
                this.nextRoomSchedule = 30;

                this.player.sendTip(this.language.translateString("admin_setRoom_setWaitSpawn"));

                item = Item.get(138);//信标
                item.setNamedTag(new CompoundTag()
                        .putInt("MurderMysteryItemType", 113));
                item.setCustomName(this.language.translateString("admin_setRoom_setWaitSpawn"));
                this.player.getInventory().setItem(4, item);
                if (!"".equals(config.getString("waitSpawn").trim())) {
                    canNext = true;
                }
                break;
            case 30: //添加随机出生点
                this.backRoomSchedule = 20;
                this.nextRoomSchedule = 40;

                this.player.sendTip(this.language.translateString("admin_setRoom_setRandomSpawn"));

                item = Item.get(138);//信标
                item.setNamedTag(new CompoundTag()
                        .putInt("MurderMysteryItemType", 113));
                item.setCustomName(this.language.translateString("admin_setRoom_setRandomSpawn"));
                this.player.getInventory().setItem(3, item);

                item = Item.get(241, 14);//红色玻璃
                item.setNamedTag(new CompoundTag()
                        .putInt("MurderMysteryItemType", 114));
                item.setCustomName(this.language.translateString("admin_setRoom_removeSetPoint"));
                this.player.getInventory().setItem(5, item);

                if (config.getStringList("randomSpawn").size() > 1) {
                    canNext = true;
                }
                break;
            case 40: //添加金锭生成点
                this.backRoomSchedule = 30;
                this.nextRoomSchedule = 50;

                this.player.sendTip(this.language.translateString("admin_setRoom_setGoldSpawn"));

                item = Item.get(41);//金块
                item.setNamedTag(new CompoundTag()
                        .putInt("MurderMysteryItemType", 113));
                item.setCustomName(this.language.translateString("admin_setRoom_setGoldSpawn"));
                this.player.getInventory().setItem(3, item);

                item = Item.get(241, 14);//红色玻璃
                item.setNamedTag(new CompoundTag()
                        .putInt("MurderMysteryItemType", 114));
                item.setCustomName(this.language.translateString("admin_setRoom_removeSetPoint"));
                this.player.getInventory().setItem(5, item);

                if (config.getStringList("goldSpawn").size() > 1) {
                    canNext = true;
                }
                break;
            case 50: //设置更多参数
                this.backRoomSchedule = 40;
                this.nextRoomSchedule = 70;
                this.player.sendTip(this.language.translateString("admin_setRoom_setMoreParameters"));
                item = Item.get(347);//钟
                item.setNamedTag(new CompoundTag().putInt("MurderMysteryItemType", 113));
                item.setCustomName(this.language.translateString("admin_setRoom_setMoreParameters"));
                this.player.getInventory().setItem(4, item);
                if (config.getInt("waitTime") > 0 &&
                        config.getInt("gameTime") > 0 &&
                        config.getInt("goldSpawnTime") > 0 &&
                        config.getInt("minPlayers") > 0 &&
                        config.getInt("maxPlayers") > 0) {
                    if (autoNext) {
                        this.setRoomSchedule(this.nextRoomSchedule);
                    }else {
                        canNext = true;
                    }
                }
                break;
            case 70: //保存设置
                this.player.sendMessage(this.language.translateString("admin_setRoom_setSuccessful"));
                config.save(true);
                this.closeEntity();
                this.owner.loadRoom(this.level.getFolderName());
                this.cancel();
                return;
        }
        //判断给 下一步/保存 物品
        if (canNext) {
            item = Item.get(340);
            if (this.nextRoomSchedule == 70) {
                item.setNamedTag(new CompoundTag()
                        .putInt("MurderMysteryItemType", 112));
                item.setCustomName(this.language.translateString("admin_setRoom_save"));
            }else {
                item.setNamedTag(new CompoundTag()
                        .putInt("MurderMysteryItemType", 111));
                item.setCustomName(this.language.translateString("admin_setRoom_next"));
            }
            this.player.getInventory().setItem(8, item);
        }else {
            this.player.getInventory().clear(8);
        }
        //显示已设置的点
        this.particleEffectTick++;
        if (this.particleEffectTick >= 10) {
            this.particleEffectTick = 0;
        }
        try{
            String[] s = config.getString("waitSpawn").split(":");
            Position pos = new Position(
                    Integer.parseInt(s[0]) + 0.5,
                    Integer.parseInt(s[1]),
                    Integer.parseInt(s[2]) + 0.5,
                    this.level);
            if (this.waitSpawnText == null || this.waitSpawnText.isClosed()) {
                this.waitSpawnText = new EntityText(pos, "§aWait §eSpawn");
                this.waitSpawnText.spawnToAll();
            }
            this.waitSpawnText.teleport(pos);
            this.particleEffect(pos);
        } catch (Exception ignored) {
        }
        try{
            List<String> randomSpawns = config.getStringList("randomSpawn");
            for (String str : new HashSet<>(this.randomSpawnTexts.keySet())) {
                if (!randomSpawns.contains(str)) {
                    EntityText entityText = this.randomSpawnTexts.get(str);
                    if (entityText != null) {
                        entityText.close();
                    }
                    this.randomSpawnTexts.remove(str);
                }
            }
            for (String str : randomSpawns) {
                String[] s = str.split(":");
                Position pos = new Position(
                        Integer.parseInt(s[0]) + 0.5,
                        Integer.parseInt(s[1]),
                        Integer.parseInt(s[2]) + 0.5,
                        this.level);
                EntityText entity = this.randomSpawnTexts.get(str);
                if (entity == null || entity.isClosed()) {
                    EntityText entityText = new EntityText(pos, "§eRandom Spawn");
                    entityText.spawnToAll();
                    this.randomSpawnTexts.put(str, entityText);
                }
                this.particleEffect(pos);
            }
        } catch (Exception ignored) {
        }
        try{
            List<String> goldSpawns = config.getStringList("goldSpawn");
            for (String str : new HashSet<>(this.goldSpawnTexts.keySet())) {
                if (!goldSpawns.contains(str)) {
                    EntityItem entityItem = this.goldSpawnTexts.get(str);
                    if (entityItem != null) {
                        entityItem.close();
                    }
                    this.goldSpawnTexts.remove(str);
                }
            }
            for (String str : goldSpawns) {
                String[] s = str.split(":");
                Position pos = new Position(
                        Integer.parseInt(s[0]) + 0.5,
                        Integer.parseInt(s[1]),
                        Integer.parseInt(s[2]) + 0.5,
                        this.level);
                EntityItem entity = this.goldSpawnTexts.get(str);
                if (entity == null || entity.isClosed()) {
                    EntityItem entityItem = new EntityItem(pos.getChunk(),
                            EntityItem.getDefaultNBT(pos)
                                    .putShort("Health", 5)
                                    .putCompound("Item", NBTIO.putItemHelper(Item.get(266)))
                                    .putShort("PickupDelay", -1)
                                    .putBoolean("cannotPickup", true));
                    entityItem.setNameTagVisible(true);
                    entityItem.setNameTagAlwaysVisible(true);
                    entityItem.setNameTag("§eGold Spawn");
                    entityItem.spawnToAll();
                    this.goldSpawnTexts.put(str, entityItem);
                }
                this.particleEffect(pos);
            }
        } catch (Exception ignored) {
        }
    }

    public int getSetRoomSchedule() {
        return this.setRoomSchedule;
    }

    public void setRoomSchedule(int setRoomSchedule) {
        this.setRoomSchedule = setRoomSchedule;
        this.player.getInventory().clear(3);
        this.player.getInventory().clear(4);
        this.player.getInventory().clear(5);
    }

    public int getBackRoomSchedule() {
        return this.backRoomSchedule;
    }

    public int getNextRoomSchedule() {
        return this.nextRoomSchedule;
    }

    public boolean isAutoNext() {
        return this.autoNext;
    }

    public void setAutoNext(boolean autoNext) {
        this.autoNext = autoNext;
    }

    private void closeEntity() {
        if (this.waitSpawnText != null) {
            this.waitSpawnText.close();
        }
        for (EntityText entityText : this.randomSpawnTexts.values()) {
            entityText.close();
        }
        for (EntityItem entityItem : this.goldSpawnTexts.values()) {
            entityItem.close();
        }
    }

    private void particleEffect(Vector3 center) {
        if (this.particleEffectTick%5 != 0) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                center.y += 0.2;
                Vector3 v = center.clone();
                v.x += 0.8;
                double x = v.x - center.x;
                double z = v.z - center.z;
                for (int i = 0; i < 360; i += 10) {
                    this.level.addParticleEffect(
                            new Vector3(
                                    x * Math.cos(i) - z * Math.sin(i) + center.x,
                                    center.y + (i * 0.0055),
                                    x * Math.sin(i) + z * Math.cos(i) + center.z),
                            ParticleEffect.REDSTONE_TORCH_DUST);
                    Thread.sleep(15);
                }
            } catch (Exception ignored) {

            }
        });
    }

    @Override
    public void onCancel() {
        this.closeEntity();
        if (this.setRoomSchedule != 70) {
            this.player.sendMessage(this.language.translateString("admin_setRoom_cancel"));
        }
        if (this.player != null) {
            this.player.getInventory().clearAll();
            this.player.getUIInventory().clearAll();
            this.player.getInventory().setContents(this.playerInventory);
            this.player.getOffhandInventory().setItem(0, this.offHandItem);
            this.player.teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
        }
        this.owner.setRoomTask.remove(this.player);
    }

}
