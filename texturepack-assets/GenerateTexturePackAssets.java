import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Generates the transparent sprites used by text-only pickup boss bars. */
public final class GenerateTexturePackAssets {

    private static final Path BOSS_BAR_DIRECTORY = Path.of(
        "mineframe-texturepack",
        "assets",
        "minecraft",
        "textures",
        "gui",
        "sprites",
        "boss_bar"
    );

    public static void main(String[] args) throws Exception {
        writeTransparentBossBar("notched_20_background.png");
        writeTransparentBossBar("white_background.png");
    }

    private static void writeTransparentBossBar(String fileName) throws Exception {
        BufferedImage image = new BufferedImage(182, 5, BufferedImage.TYPE_INT_ARGB);
        Path path = BOSS_BAR_DIRECTORY.resolve(fileName);
        Files.createDirectories(path.getParent());
        ImageIO.write(image, "png", path.toFile());
    }
}

