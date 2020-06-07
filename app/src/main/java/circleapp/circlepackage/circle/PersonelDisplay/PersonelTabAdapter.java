package circleapp.circlepackage.circle.PersonelDisplay;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import circleapp.circlepackage.circle.ObjectModels.Circle;


public class PersonelTabAdapter extends FragmentPagerAdapter {

    private int numberOfTabs;
    private String userState;

    public PersonelTabAdapter(@NonNull FragmentManager fm, int behavior,String userState) {
        super(fm);
        this.numberOfTabs = behavior;
        this.userState = userState;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AllMembersFragment();
            case 1:
                return new ApplicantDisplayFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
