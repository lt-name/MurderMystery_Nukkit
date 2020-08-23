package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntitySword;
import cn.lanink.murdermystery.room.BaseRoom;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;

import java.util.LinkedList;

public class SwordMoveTask extends AsyncTask {

    private final BaseRoom room;
    private final Player player;
    private LinkedList<double[]> math;
    private EntitySword sword;

    public SwordMoveTask(BaseRoom room, Player player) {
        this.room = room;
        this.player = player;
        Position pos1 = new Position(player.x, player.y + player.getEyeHeight(), player.z, player.getLevel());
        Position pos2 = player.getTargetBlock(15) == null ? null : player.getTargetBlock(15).getLocation();
        if (pos2 == null) {
            return;
        }
        pos2.x += 0.5D;
        pos2.y += 0.5D;
        pos2.z += 0.5D;
        this.math = this.mathLine(pos1, pos2);
        if (this.math == null || this.math.size() == 0) {
            return;
        }
        Skin skin = MurderMystery.getInstance().getSword();
        CompoundTag tag = EntitySword.getDefaultNBT(Location.fromObject(pos1, player.getLevel(), player.getYaw(), player.getPitch()));
        tag.putCompound("Skin",new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        tag.putFloat("Scale", 0.5F);
        this.sword = new EntitySword(player.getChunk(), tag);
        this.sword.setSkin(skin);
        this.sword.setRotation(player.getYaw(), player.getPitch());
        this.sword.spawnToAll();
        Tools.setHumanSkin(this.sword, skin);
    }

    @Override
    public void onRun() {
        try {
            for (double[] position : this.math) {
                Position p = new Position(position[0], position[1], position[2], this.sword.level);
                Thread.sleep(60);
                this.sword.setPosition(p);
                this.sword.updateMovement();
                for (Entity entity : p.level.getEntities()) {
                    if (entity instanceof Player) {
                        Player player2 = (Player) entity;
                        if (player2 == this.player) {
                            continue;
                        }
                        if (p.distance(entity) < 1.5 || p.distance(new Vector3(entity.x, entity.y + entity.getHeight(), entity.z)) < 1.5) {
                            if (MurderMystery.debug) {
                                Server.getInstance().getLogger().info("距离：" + p.distance(entity));
                                Server.getInstance().getLogger().info("距离（加高度）：" + p.distance(new Vector3(entity.x, entity.y + entity.getHeight(), entity.z)));
                            }
                            this.room.playerDamageEvent(this.player, player2);
                            this.sword.close();
                            return;
                        }
                    }
                }
            }
        } catch (Exception ignored) {

        }
        this.sword.close();
    }

    private LinkedList<double[]> mathLine(Position pos1, Position pos2) {
        if (pos1 != null && pos2 != null) {
            LinkedList<double[]> positions = new LinkedList<>();
            double dis = Math.sqrt(Math.pow(pos1.x - pos2.x, 2.0D) +
                    Math.pow(pos1.y - pos2.y, 2.0D) +
                    Math.pow(pos1.z - pos2.z, 2.0D));
            for (double t = 0.0D; t <= 1.0D; t += 1.0D / dis) {
                positions.add(new double[]{pos1.x + (pos2.x - pos1.x) * t,
                        pos1.y + (pos2.y - pos1.y) * t,
                        pos1.z + (pos2.z - pos1.z) * t});
            }
            return positions;
        }
        return null;
    }

}
