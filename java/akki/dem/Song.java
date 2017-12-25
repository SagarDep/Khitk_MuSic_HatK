package akki.dem;

/**
 * Created by DELL on 07-09-2016.
 */


public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private Integer year;


    public Song(long songID, String songTitle, String songArtist, String songAlbum , Integer songYear ) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        year = songYear;
    }


    public long getID(){
        return id;}
    public String getTitle(){
        return title;}
    public String getArtist(){
        return artist;}
    public String getAlbum(){
        return album;}
    public Integer getYear() {
        return year;}


}
