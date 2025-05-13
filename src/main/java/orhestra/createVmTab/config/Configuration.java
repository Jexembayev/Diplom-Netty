package orhestra.createVmTab.config;

/**
 * Конфигурация, загружаемая из .ini файла.
 * Содержит только imageConf, так как используется только деплой по образу.
 */
public class Configuration {
    private General generalConf;
    private Image imageConf;

    public General getGeneralConf() {
        return generalConf;
    }

    public void setGeneralConf(General generalConf) {
        this.generalConf = generalConf;
    }

    public Image getImageConf() {
        return imageConf;
    }

    public void setImageConf(Image imageConf) {
        this.imageConf = imageConf;
    }
}

