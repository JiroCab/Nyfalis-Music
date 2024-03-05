package NyfalisMusic;

import arc.Core;
import arc.Events;
import arc.audio.Music;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog;

import static mindustry.Vars.*;

public class NyfMusicMain extends Mod {
    public static Music
            reclaiming_the_wasteland = new Music(),
            dusk = new Music(),
            blossom = new Music(),
            feu = new Music(),
            main_title = new Music(),
            sparkles_of_hope = new Music()
    ;

    public boolean nyfalisMusicSet = false, nyfalisPresent;
    public static Seq<Music> previousAmbientMusic, previousBossMusic, previousDarkMusic;

    public Seq<Music>
            nyfalisAmbient = new Seq<>(),
            nyfalisBoss = new Seq<>(),
            nyfalisDark = new Seq<>();


    @Override
    public void loadContent(){
        LoadMusic();

        Log.info("UwO Nyfalis music Loaded.");
    }

    public NyfMusicMain(){
        if(headless) return;
        Events.on(EventType.ClientLoadEvent.class, e ->{
            nyfalisCheck();
            buildDialog();

            nyfalisAmbient.add(reclaiming_the_wasteland, blossom, feu, main_title);
            nyfalisDark.add(reclaiming_the_wasteland, dusk, sparkles_of_hope);
            nyfalisBoss.add(dusk, sparkles_of_hope);

            if(!nyfalisPresent)buildNyfalisSoundSettings(Vars.ui.settings.sound, true);
        });
        Events.on(EventType.WorldLoadEvent.class, l -> replaceSoundHandler());
    }


    public static void LoadMusic(){
        if(headless) return;
        Core.assets.load("music/reclaiming_the_wasteland.mp3", arc.audio.Music.class).loaded = a -> reclaiming_the_wasteland = a;
        Core.assets.load("music/dusk.mp3", arc.audio.Music.class).loaded = a -> dusk = a;
        Core.assets.load("music/blossom.mp3", arc.audio.Music.class).loaded = a -> blossom = a;
        Core.assets.load("music/feu.mp3", arc.audio.Music.class).loaded = a -> feu = a;
        Core.assets.load("music/main_title.mp3", arc.audio.Music.class).loaded = a -> main_title = a;
        Core.assets.load("music/sparkles_of_hope.mp3", arc.audio.Music.class).loaded = a -> sparkles_of_hope = a;
    }

    /*least invasive approach, hopefully a mod that changes music still has the Seqs public*/
    public void replaceSoundHandler(){
        if(shouldReplaceMusic()){
            if(nyfalisMusicSet) return;

            previousAmbientMusic = control.sound.ambientMusic.copy();
            previousDarkMusic = control.sound.darkMusic.copy();
            previousBossMusic = control.sound.bossMusic.copy();

            if (Core.settings.getBool("nyfalis-music-add")){
                control.sound.ambientMusic.clear();
                control.sound.darkMusic.clear();
                control.sound.bossMusic.clear();
            }

            control.sound.darkMusic.add(nyfalisAmbient);
            control.sound.ambientMusic.add(nyfalisDark);
            control.sound.bossMusic.add(nyfalisBoss);

            nyfalisMusicSet = true;
            if (Core.settings.getBool("nyfalis-music-add")) Log.info("Nyfalis Music replaced SoundControl's music Seq(s)!");
            else Log.info("Nyfalis added custom music to SoundControl");
        }else if (nyfalisMusicSet){
            control.sound.ambientMusic.clear().addAll(previousAmbientMusic );
            control.sound.ambientMusic.clear().addAll(previousAmbientMusic );
            control.sound.bossMusic.clear().addAll(previousBossMusic);
            Log.info("Nyfalis Music Restored the previous SoundControl's music Seq(s)!");
        }
    }

    public boolean shouldReplaceMusic(){
        if (Core.settings.getBool("nyfalis-music-only")) return true;
        if (Core.settings.getBool("nyfalis-music-add")) return true;
        if(!nyfalisPresent) return false;
        //Handle by the content mod so i don't have to do checks here
        return Core.settings.getBool("nyfalis-replacemusic");
    }

    public void buildDialog(){
        if(nyfalisPresent) return; //Content mod handles it

        ui.settings.addCategory("@category.nyfalis.name", Icon.effect, table -> {
            buildNyfalisSoundSettings(table, false);
        });
    }

    public static void  buildNyfalisSoundSettings(Table table, Boolean hide){
        if(!Core.settings.getBool("nyfalis-hide-sound") && hide)return;

        boolean[] shown = {!hide};
        table.row();
        table.button("@setting.nyfalis-sound-category", Icon.effect, Styles.togglet, () -> shown[0] = !shown[0]).margin(14f).growX().height(60f).checked(a -> shown[0]).pad(5f).center().row();

        table.collapser(t -> {
            SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
            if (!hide) subTable.checkPref("nyfalis-hide-sound", true);
            else  t.label(() -> "@settings.nyfalis-music-hint").top().center().padBottom(5f).margin(3).row();

            subTable.checkPref("nyfalis-music",true);
            subTable.checkPref("nyfalis-music-add",true, c -> Core.settings.put("nyfalis-replacemusic", false));
            t.add(subTable).row();
            t.label(() -> "@settings.nyfalis-music-content").bottom().center().padBottom(5f).margin(3).row();
        }, () ->shown[0]).growX().center().row();
    }

    public void nyfalisCheck(){
        nyfalisPresent = mods.locateMod("olupis") != null;
        if(nyfalisPresent) Log.info("Nyfalis Music has heard Nyfalis Nya-ing!");
    }

}
