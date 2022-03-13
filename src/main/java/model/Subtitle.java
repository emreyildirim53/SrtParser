package model;

public class Subtitle
{
    int index;
    String time;
    String text;
    double startSecond;
    double endSecond;

    public Subtitle(int index, String time, String text, double startSecond, double endSecond)
    {
        this.index = index;
        this.time = time;
        this.text = text;
        this.startSecond = startSecond;
        this.endSecond = endSecond;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public double getStartSecond()
    {
        return startSecond;
    }

    public void setStartSecond(double startSecond)
    {
        this.startSecond = startSecond;
    }

    public double getEndSecond()
    {
        return endSecond;
    }

    public void setEndSecond(double endSecond)
    {
        this.endSecond = endSecond;
    }
}
