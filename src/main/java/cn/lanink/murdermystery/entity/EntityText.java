package cn.lanink.murdermystery.entity;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityText extends Entity {

    public int getNetworkId() {
        return 64;
    }

    public EntityText(FullChunk chunk, CompoundTag nbt, Player player) {
        super(chunk, nbt);
        this.setNameTagVisible(true);
        this.setNameTagAlwaysVisible(true);
        this.setNameTag("§e╭───╮\n§e|  §c!  §e|\n§e╰───╯");
        this.setPosition(new Vector3(player.x, player.y + 1, player.z));
    }

    protected void initEntity() {
        super.initEntity();
        setMaxHealth(20);
        setHealth(20.0F);
        setNameTagVisible(true);
        setNameTagAlwaysVisible(true);
        setImmobile();
    }

}
