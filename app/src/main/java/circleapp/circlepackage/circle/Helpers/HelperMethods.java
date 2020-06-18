package circleapp.circlepackage.circle.Helpers;

import java.util.List;

import circleapp.circlepackage.circle.ObjectModels.Circle;

public class HelperMethods {

    public static int returnIndexOfCircleList(List<Circle> circleList, Circle circle){
        int position = 0;
        for(Circle c : circleList){
            if(c.getId().equals(circle.getId()))
                return position;

            position++;
        }
        return position;
    }

    public static boolean isMemberOfCircle (Circle circle, String uID){
        boolean isMember = false;
        if(circle.getMembersList()!=null){
            for(String memberId : circle.getMembersList().keySet()){
                if(memberId.equals(uID))
                    isMember = true;
            }
        }
        return isMember;
    }

    public static boolean listContainsCircle (List<Circle> circleList, Circle circle){
        boolean containsCircle = false;
        for(Circle c : circleList){
            if(c.getId().equals(circle.getId()))
                containsCircle = true;
        }
        return containsCircle;
    }
}
