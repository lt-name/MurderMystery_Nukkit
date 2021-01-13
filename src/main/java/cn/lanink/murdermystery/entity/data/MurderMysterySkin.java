package cn.lanink.murdermystery.entity.data;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.ItemMap;

import java.awt.image.BufferedImage;

/**
 * @author lt_name
 */
public class MurderMysterySkin extends Skin {

    BufferedImage wantedImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);

    public MurderMysterySkin() {
        super();
    }

    public MurderMysterySkin(Skin skin) {
        super();
        this.setSkinData(skin.getSkinData());
        this.setGeometryName(skin.getSkinResourcePatch());
        this.setGeometryData(skin.getGeometryData());
        this.setSkinResourcePatch(skin.getSkinResourcePatch());
    }

    public void setWantedImage(BufferedImage wantedImage) {
        this.wantedImage = wantedImage;
    }

    public BufferedImage getWantedImage() {
        return this.wantedImage;
    }

    public ItemMap getItemMap() {
        ItemMap item = new ItemMap();
        item.setImage(this.getWantedImage());
        return item;
    }

}
