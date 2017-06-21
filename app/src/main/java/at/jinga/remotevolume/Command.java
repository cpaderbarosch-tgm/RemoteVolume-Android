package at.jinga.remotevolume;

public class Command {
    public Action Action;
    public Integer Id;
    public boolean Mute;
    public float Volume;

    public Command(Action Action, Integer Id, boolean Mute, float Volume) {
        this.Action = Action;
        this.Id = Id;
        this.Mute = Mute;
        this.Volume = Volume;
    }
}