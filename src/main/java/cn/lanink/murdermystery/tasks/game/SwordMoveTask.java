package cn.lanink.murdermystery.tasks.game;

import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.entity.EntitySword;
import cn.lanink.murdermystery.event.MurderPlayerDamageEvent;
import cn.lanink.murdermystery.room.RoomBase;
import cn.lanink.murdermystery.utils.Tools;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;

import java.util.LinkedList;

public class SwordMoveTask extends AsyncTask {

    private final RoomBase room;
    private final Player player;
    private LinkedList<double[]> math;
    private EntitySword sword;

    public SwordMoveTask(RoomBase room, Player player) {
        this.room = room;
        this.player = player;
        Position pos1 = new Position(player.x + 0.5D, player.y + player.getEyeHeight(), player.z + 0.5D, player.getLevel());
        Position pos2 = player.getTargetBlock(15) == null ? null : player.getTargetBlock(15).getLocation();
        if (pos2 == null) return;
        this.math = this.mathLine(pos1, pos2);
        if (this.math == null || this.math.size() == 0) return;
        Skin skin = MurderMystery.getInstance().getSword();
        CompoundTag tag = EntitySword.getDefaultNBT(pos1);
        tag.putCompound("Skin",new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId", skin.getSkinId()));
        tag.putFloat("Scale", 0.5F);
        this.sword = new EntitySword(player.getChunk(), tag);
        Tools.setHumanSkin(this.sword, skin);
        this.sword.setRotation(player.getYaw(), player.getPitch());
        this.sword.spawnToAll();
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
                        if (entity.x >= p.x - entity.getWidth() && entity.x <= p.x + entity.getWidth() &&
                                entity.y >= p.y - entity.getHeight() && p.y <= entity.y + entity.getHeight() &&
                                entity.z >= p.z - entity.getWidth() && entity.z <= p.z + entity.getWidth()) {
                            Player player2 = (Player) entity;
                            if (player2 != this.player) {
                                Server.getInstance().getPluginManager().callEvent(
                                        new MurderPlayerDamageEvent(this.room, this.player, player2));
                                this.sword.close();
                                return;
                            }
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
