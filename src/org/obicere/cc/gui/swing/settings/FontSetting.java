package org.obicere.cc.gui.swing.settings;

import org.obicere.cc.util.FontUtils;
import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * @author Obicere
 */
public class FontSetting extends SettingPanel {

    public FontSetting(SettingsShutDownHook hook, String value, String description) {
        super(hook, value, description);
    }

    @Override
    protected void buildPanel() {

        final SettingsShutDownHook hook = getHook();
        final String key = getKey();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        final JLabel description = new JLabel(getDescriptor());
        final JComboBox<String> fontSelection = new JComboBox<>(FontUtils.getLoadedFonts());

        final String selectedFont = hook.getPropertyAsString(key);
        if (FontUtils.isFont(selectedFont)) {
            fontSelection.setSelectedItem(hook.getPropertyAsString(key));
        } else {
            fontSelection.setSelectedItem(hook.getDefaultValue(key));
        }
        fontSelection.addItemListener(e -> {
            final String selected = (String) e.getItem();
            hook.setProperty(key, selected);
        });

        add(description);
        add(Box.createHorizontalGlue());
        add(fontSelection);
    }
}
