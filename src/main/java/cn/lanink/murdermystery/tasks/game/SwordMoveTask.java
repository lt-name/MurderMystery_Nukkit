package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntitySword;
import cn.lanink.murdermystery.room.RoomBase;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;

import java.util.LinkedList;

public class SwordMoveTask extends AsyncTask {

    private final RoomBase room;
    private final Player player;
    private LinkedList<double[]> math;
    private EntitySword sword;
    private double yaw, pitch;

    public SwordMoveTask(RoomBase room, Player player) {
        this.room = room;
        this.player = player;
        this.yaw = player.getYaw();
        this.pitch = player.getPitch();
        Position pos1 = new Position(player.x, player.y + player.getEyeHeight(), player.z, player.getLevel());
        Position pos2 = player.getTargetBlock(15) == null ? null : player.getTargetBlock(15).getLocation();
        if (pos2 == null) return;
        this.math = this.mathLine(pos1, pos2);
        if (this.math == null || this.math.size() == 0) return;
        Skin skin = MurderMystery.getInstance().getSword();
        CompoundTag tag = EntitySword.getDefaultNBT(Location.fromObject(pos1, player.getLevel(), player.getYaw(), player.getPitch()));
        tag.putCompound("Skin",new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        tag.putFloat("Scale", 0.5F);
        this.sword = new EntitySword(player.getChunk(), tag);
        this.sword.setSkin(skin);
        this.sword.setRotation(this.yaw, this.pitch);
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
                        if (player2 == this.player) continue;
                        if (((entity.x - entity.getWidth() - 0.5) <= p.x) && ((entity.x + entity.getWidth() + 0.5) >= p.x) &&
                                ((entity.y - entity.getWidth() - 0.5) <= p.y) && ((entity.x + entity.getWidth() + 0.5) >= p.y) &&
                                ((entity.z - entity.getWidth() - 0.5) <= p.z) && ((entity.z + entity.getWidth() + 0.5) >= p.z)) {
                            this.room.playerDamageEvent(this.player, player2);
                            this.sword.close();
                            return;
                        }
                    }
                }
            }
        } catch (InterruptedException ignored){ }
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
