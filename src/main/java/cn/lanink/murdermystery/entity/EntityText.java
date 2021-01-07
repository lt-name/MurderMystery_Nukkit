package cn.lanink.murdermystery.entity;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityText extends Entity {

    public int getNetworkId() {
        return 64;
    }

    public EntityText(Position position, String message) {
        super(position.getChunk(), getDefaultNBT(position));
        this.setNameTag(message);
    }

    public EntityText(FullChunk chunk, CompoundTag nbt, Player player) {
        super(chunk, nbt);
        this.setNameTag("§e╭───╮\n§e|  §c!  §e|\n§e╰───╯");
        this.setPosition(new Vector3(player.x, player.y + 1, player.z));
    }

    protected void initEntity() {
        super.initEntity();
        this.setMaxHealth(20);
        this.setHealth(20.0F);
        this.setNameTagVisible(true);
        this.setNameTagAlwaysVisible(true);
        this.setImmobile();
    }

}
