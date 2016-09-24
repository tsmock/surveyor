// License: GPL. For details, see LICENSE file.
package at.dallermassl.josm.plugin.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.io.OsmWriterFactory;

/**
 * @author cdaller
 *
 */
public class AutoSaveEditLayerTimerTask extends TimerTask {
    private File file;

    public AutoSaveEditLayerTimerTask(String filename) {
        file = new File(filename);
    }

    @Override
    public void run() {
        OsmDataLayer layer = Main.getLayerManager().getEditLayer();
        if (layer == null) {
            return;
        }
        try {
            DataSet dataset = layer.data;

            // write to temporary file, on success, rename tmp file to target file:
            File tmpFile = new File(file.getAbsoluteFile()+".tmp");
            System.out.println("AutoSaving osm data to file " + file.getAbsolutePath());
            synchronized (SurveyorLock.class) {
                OsmWriter w = OsmWriterFactory.createOsmWriter(new PrintWriter(new FileOutputStream(tmpFile)), false, dataset.getVersion());
                w.header();
                w.writeDataSources(dataset);
                w.writeContent(dataset);
                w.footer();
            }
            tmpFile.renameTo(file);
            System.out.println("AutoSaving finished");
        } catch (IOException x) {
            x.printStackTrace();
            JOptionPane.showMessageDialog(Main.parent,
                tr("Error while exporting {0}: {1}", file.getAbsoluteFile(), x.getMessage()),
                tr("Error"),
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
