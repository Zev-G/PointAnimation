package json;

public interface JSONSavable<J> {

    void apply(J json);
    J toJSON();

}
