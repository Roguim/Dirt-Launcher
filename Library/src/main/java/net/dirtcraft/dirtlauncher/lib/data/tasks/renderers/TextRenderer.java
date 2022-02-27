package net.dirtcraft.dirtlauncher.lib.data.tasks.renderers;

import net.dirtcraft.dirtlauncher.lib.util.DataFormat;

public class TextRenderer implements Renderer {
    int barLength = 25;

    @Override
    public void apply(String title, long current, long max, long bitRate) {
        double percent = Math.min((double) current / max, 1D);
        String barFilled = getSolid((int) (percent * barLength));
        if (barFilled.length() < barLength) barFilled += getFractional(percent);
        DataFormat format = DataFormat.getMaximumDataRate(max);
        System.out.printf("\r%s: |%-25s| %s/%s (%s) %25s",
                title,
                barFilled,
                format.toFileSize(current),
                format.toFileSize(max),
                DataFormat.getBitrate(bitRate),
                "");
    }

    private String getSolid(int amount) {
        return new String(new char[amount]).replace("\0", "█");
    }

    private char getFractional(double percent) {
        int last = (int) (percent * (barLength << 3)) & 7;
        switch (last) {
            case 0: return '▏';
            case 1: return '▎';
            case 2: return '▍';
            case 3: return '▌';
            case 4: return '▋';
            case 5: return '▊';
            case 6: return '▉';
            case 7: return '█';
            default: return ' ';
        }
    }
}
