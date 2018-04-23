package myapplication.t.example.com.weixin;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.util.List;

public class ThreeFragment extends Fragment {
    private PersonDao personDao;
    private ListView lvPersons;
    private View view;
    private PersonAdapter personAdapter;
    private List<Word> persons;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_dcb, container, false);
        lvPersons = (ListView) view.findViewById(R.id.lvPersons);
//        personDao = new PersonDao(getContext());
//        //任务 查询数据库中的所有person，显示在列表中
//        persons = personDao.getPersons();
//        personAdapter = new PersonAdapter(getContext(), R.layout.fragment_item, persons);
//        lvPersons.setAdapter(personAdapter);

        return view;
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            personDao = new PersonDao(getContext());
            //任务 查询数据库中的所有person，显示在列表中
            persons = personDao.getPersons();
            personAdapter = new PersonAdapter(getContext(), R.layout.fragment_item, persons);
            lvPersons.setAdapter(personAdapter);
            personAdapter.notifyDataSetChanged();
            //逻辑
        }
    }
}
