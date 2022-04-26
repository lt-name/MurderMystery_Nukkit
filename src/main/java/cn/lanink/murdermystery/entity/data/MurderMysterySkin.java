package cn.lanink.murdermystery.entity.data;

import cn.lanink.murdermystery.item.ItemManager;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMap;
import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;

/**
 * @author lt_name
 */
public class MurderMysterySkin extends Skin {

    @Setter
    @Getter
    private BufferedImage wantedImage =
            new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);

    public MurderMysterySkin() {
        super();
    }

    public MurderMysterySkin(Skin skin) {
        super();
        this.setSkinData(skin.getSkinData());
        this.setGeometryName(skin.getSkinResourcePatch());
        this.setGeometryData(skin.getGeometryData());
    }

    public ItemMap getItemMap() {
        ItemMap item = (ItemMap) Item.get(Item.MAP, 0, 1);
        item.setImage(this.getWantedImage());
        item.getNamedTag()
                .putBoolean(ItemManager.IS_MURDER_MYSTERY_TAG, true)
                .putBoolean("cannotClickOnInventory", true);
        return item;
    }

}
