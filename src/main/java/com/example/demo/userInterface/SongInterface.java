package com.example.demo.userInterface;

import com.example.demo.entity.Song;
import com.example.demo.entity.SongList;
import com.example.demo.entity.User;
import com.example.demo.entity.result.ResultEntity;
import com.example.demo.service.KeepService;
import com.example.demo.service.SongListService;
import com.example.demo.service.UserService;
import com.example.demo.util.AutoShowUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SongInterface {
    @Autowired
    private UserService userService;
    @Autowired
    private SongListService songListService;
    @Autowired
    private AutoShowUtil showUtil;
    @Autowired
    private KeepService keepService;

    @RequestMapping(value = "/profile/like_song_song_typeList", method = RequestMethod.GET)
    public ModelAndView showSongsInList(HttpServletRequest request, HttpServletResponse response){
        String id = request.getParameter("id");
        String flag = request.getParameter("flag");
        ResultEntity e;
        Map<String,Object> map = new HashMap<>();
        User user = new User();
        user.setUserid(id);
        e = userService.getSongLists(user);
        Map<String,Object>e_map = (Map<String, Object>) e.getObject();
        ArrayList<SongList> createdsonglist = (ArrayList<SongList>)e_map.get("createdsonglist");
        getFavoriteList(map,createdsonglist);
        ArrayList<Song> songs = (ArrayList<Song>) map.get("songs");
        map.putAll(showUtil.showSong(songs));
        if(flag.equals("2"))
            return new ModelAndView("temp/mylike/song_list_details",map);
        return new ModelAndView("temp/mylike_main",map);
    }
    @RequestMapping(value = "/favoriteSong",method = RequestMethod.POST)
    public ResultEntity favoriteSong(@Param("songid")String songid, HttpServletRequest request){
        User user = (User) request.getSession(false).getAttribute("user");
        SongList favorite = userService.getFavoritelist(user.getUserid());
        boolean succ = true;
        if(favorite == null){
            succ = false;
            return new ResultEntity(succ,"用户默认歌单不存在",null);
        }
        String result = keepService.KeepSong(songid,favorite.getSonglistid());
        if(result.equals("0")){
            succ = false;
            return new ResultEntity(succ,"保存失败,歌曲已存在",null);
        }
        return new ResultEntity(succ,"喜欢成功",null);
    }
    @RequestMapping(value = "/KeepSong",method = RequestMethod.POST)
    public ResultEntity KeepSong(@Param("songlistid")String songlistid, @Param("songid")String songid,
                                 HttpServletRequest request){
        String result = keepService.KeepSong(songid,songlistid);
        boolean succ = true;
        if(result.equals("0")){
            succ = false;
            return new ResultEntity(succ,"保存失败,歌曲已存在",null);
        }
        return new ResultEntity(succ,"保存成功",null);
    }
    private void getFavoriteList(Map<String,Object> map,ArrayList<SongList> createdsonglist){
        ResultEntity e;
        for(SongList i : createdsonglist){
            if(i.getSonglistname().equals("我喜欢")){
                map.put("favoritesonglist",i);
                e = songListService.getSongsInSongList(i);
                map.put("songs",e.getObject());
                break;
            }
        }
    }
}
