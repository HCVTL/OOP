package com.ChronosDetective.game.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public final class FontUtils {
    private FontUtils() {}

    public static BitmapFont createUnicodeFont(int sizePx) {
        try {
            FileHandle ttf = resolveTtfFile();
            if (ttf == null || !ttf.exists()) return new BitmapFont();

            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(ttf);
            try {
                FreeTypeFontParameter p = new FreeTypeFontParameter();
                p.size = sizePx;
                p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                        + "ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơ"
                        + "ƯăẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂỄỆ"
                        + "ỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪỬỮỰ"
                        + "ỳýỵỷỹỲÝỴỶỸ"
                        + "“”‘’…–—•";
                p.color = Color.WHITE;
                p.incremental = false;
                return gen.generateFont(p);
            } finally {
                gen.dispose();
            }
        } catch (Exception ignored) {
            return new BitmapFont();
        }
    }

    private static FileHandle resolveTtfFile() {
        FileHandle f = Gdx.files.internal("fonts/NotoSans-Regular.ttf");
        if (f.exists()) return f;

        f = Gdx.files.internal("fonts/Roboto-Regular.ttf");
        if (f.exists()) return f;

        // Desktop fallback on Windows: use system font if project font not provided yet.
        f = Gdx.files.absolute("C:/Windows/Fonts/arial.ttf");
        if (f.exists()) return f;

        f = Gdx.files.absolute("C:/Windows/Fonts/tahoma.ttf");
        if (f.exists()) return f;

        return null;
    }
}
