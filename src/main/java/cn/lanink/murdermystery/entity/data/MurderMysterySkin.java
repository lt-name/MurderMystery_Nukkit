package cn.lanink.murdermystery.entity.data;

import cn.nukkit.entity.data.Skin;

import java.awt.image.BufferedImage;

/**
 * @author lt_name
 */
public class MurderMysterySkin extends Skin {

    BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }

}
