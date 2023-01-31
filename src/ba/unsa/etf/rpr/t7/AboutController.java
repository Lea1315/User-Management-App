package ba.unsa.etf.rpr.t7;

import javafx.event.ActionEvent;
import javafx.scene.control.Hyperlink;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AboutController {
    public Hyperlink hypGithub;

    public AboutController() {
        hypGithub = new Hyperlink();
    }

    public void actionLink(ActionEvent actionEvent) {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI("https://github.com/Lea1315"));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
    }
}
