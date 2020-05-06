package com.example.my_sensor;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

public class find_path {

    char map[][] = {
            // 0  1   2   3   4   5   6   7   8   9   10  11  12  13  14  15  16  17  18  19  20  21 22  23  24  25  26  27  28
            {'x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','o','o','o','5','x','x','x','x','x','x','x','x'}, //0
            {'x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','o','o','o','x','x','x','x','x','x','x','x','x'}, //1
            {'x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','x','o','o','o','x','x','x','x','x','x','x','x','x'}, //2
            {'x','x','x','x','x','x','3','x','x','x','x','x','x','x','x','x','x','o','o','o','o','o','o','o','o','o','1','x','x'}, //3
            {'o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','x','x','x','o','o','o','o','o','o','o'}, //4
            {'o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','x','x','x','o','o','o','o','x','x','x'}, //5
            {'o','o','x','x','o','o','x','x','o','o','x','x','o','o','o','o','o','o','o','x','x','x','o','o','o','o','x','x','x'}, //6
            {'o','o','x','x','o','o','x','x','o','o','x','x','o','o','o','o','o','o','o','x','x','x','o','o','o','o','x','x','x'}, //7
            {'o','o','x','x','o','o','x','x','o','o','x','x','o','o','o','o','o','o','o','x','x','x','o','o','o','o','x','x','x'}, //8
            {'o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','o','2','2'}, //9
            {'4','4','4','4','4','4','4','4','4','4','4','4','4','4','4','4','4','x','x','x','x','x','x','s','x','x','x','x','x'}}; //10

    Context context;
    ArrayList<path> path_v;

    private int current_x=0;
    private int current_y=0;

    public find_path(Context context1){
        context = context1;
        set_path();
    }

    public void set_current_position(int y1, int x1){
        current_y = y1;
        current_x = x1;
    }
    public int getCurrent_x(){
        return  current_x;
    }
    public int getCurrent_y(){
        return current_y;
    }

    public int move_front(){
        int x = current_x;
        int y = current_y;

        int i;
        path tmp;
        int flag;

        for(i =0 ; i < path_v.size(); i ++) {
            tmp = path_v.get(i);
            Log.e("hh",i+": "+tmp.get_x()+","+tmp.get_y());
            if((tmp.get_x() == x) && (tmp.get_y() == y)){
                flag = 1;
                break;
            }
        }
        if(i+1 >= path_v.size()){ // 목적지 도착
            return -1;
        }
        current_x = path_v.get(i+1).get_x();
        current_y = path_v.get(i+1).get_y();

        return 0; // 이동 성공
    }


    public void set_path(){
        //화장실 가는길.
        path_v = new ArrayList<path>();
        path_v.add(new path(10, 23));
        path_v.add(new path(9, 23));
        path_v.add(new path(8, 23));
        path_v.add(new path(7, 23));
        path_v.add(new path(6, 23));
        path_v.add(new path(5, 23));
        path_v.add(new path(4, 23));
        path_v.add(new path(3, 23));
        path_v.add(new path(3, 22));
        path_v.add(new path(3, 21));
        path_v.add(new path(3, 20));
        path_v.add(new path(3, 19));
        path_v.add(new path(2, 19));
        path_v.add(new path(1, 19));
        path_v.add(new path(0, 19));
        path_v.add(new path(0, 20));
    }

    public int find_next_move(){
        int x = current_x;
        int y = current_y;

        path tmp;
        int flag = 0;
        int i;
        for(i =0 ; i < path_v.size(); i ++) {
            tmp = path_v.get(i);
            Log.e("hh",i+": "+tmp.get_x()+","+tmp.get_y());
            if((tmp.get_x() == x) && (tmp.get_y() == y)){
                flag = 1;
                break;
            }
        }
        if(flag==1){
            if((i-1) == path_v.size()){
                Log.e("hh","1");

                return 0;
            }
            else{
                Log.e("hh","2");
                //다음 길을 탐색
                if(i+1 >= path_v.size())
                    return -1;
                tmp = path_v.get(i+1);
                if(tmp.get_y() == y && tmp.get_x() == x+1)
                    return 3; //오른쪽으로 이동
                if(tmp.get_y() == y && tmp.get_x() == x-1)
                    return 1; //왼쪽으로이동
                if(tmp.get_y() == y-1 && tmp.get_x() == x)
                    return 2; //앞으로이동
                if(tmp.get_y() == y+1 && tmp.get_x() == x)
                    return 4;
            }
        }
        else
            return -1;
        Log.e("hh","3");
        return 0;
    }

    public String find_direction(float maz){ //방향이 일치하면 앞으로 이동하라가 출력, 일치하지 않으면 방향을 이동하라 출력
        int next_direction = find_next_move();
        String msg;

        if(next_direction == -1)
            return "목적지에 도착하였습니다.";
        if(next_direction == 1){ //서쪽
            if(maz > 240 && maz <290){
                msg = "올바른 방향입니다. 앞으로 이동하세요";
                return msg;
            }
            else if(maz >= 290 || maz < 90){
                msg = "올바르지 않은 방향입니다. 왼쪽으로 바라보는 방향을 서서히 움직이세요.";
                return msg;
            }
            else if(maz >= 90 && maz <= 240){
                msg = "올바르지 않은 방향입니다. 오른쪽으로 바라보는 방향을 서서히 움직이세요.";
                return msg;
            }
        }
        if(next_direction == 2){ //북쪽
            if(maz > 340 || maz <20){
                msg = "올바른 방향입니다. 앞으로 이동하세요";
                return msg;
            }
            else if(maz >= 20 && maz < 180){
                msg = "올바르지 않은 방향입니다. 왼쪽으로 바라보는 방향을 서서히 움직이세요.";
                return msg;
            }
            else if(maz >= 180 && maz <= 340){
                msg = "올바르지 않은 방향입니다. 오른쪽으로 바라보는 방향을 서서히 움직이세요.";
                return msg;
            }
        }

        if(next_direction == 3){ //동쪽
            if(maz > 70 && maz <110){
                msg = "올바른 방향입니다. 앞으로 이동하세요";
                return msg;
            }
            else if(maz >= 110 && maz < 270){
                msg = "올바르지 않은 방향입니다. 왼쪽으로 바라보는 방향을 서서히 움직이세요.";
                return msg;
            }
            else if(maz >= 270 || maz <= 70){
                msg = "올바르지 않은 방향입니다. 오른쪽으로 바라보는 방향을 서서히 움직이세요.";
                return msg;
            }
        }

        if(next_direction == 4){ //남쪽
            if(maz > 160 && maz <200){
                msg = "올바른 방향입니다. 앞으로 이동하세요";
                return msg;
            }
            else if(maz >= 200){
                msg = "올바르지 않은 방향입니다. 왼쪽으로 바라보는 방향을 서서히 움직이세요.";
                return msg;
            }
            else if(maz <= 160){
                msg = "올바르지 않은 방향입니다. 오른쪽으로 바라보는 방향을 서서히 움직이세요.";
                return msg;
            }
        }

        return "error";
    }

}

