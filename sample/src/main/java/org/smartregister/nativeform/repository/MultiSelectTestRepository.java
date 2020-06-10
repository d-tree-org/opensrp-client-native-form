package org.smartregister.nativeform.repository;

import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.interfaces.MultiSelectListRepository;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectTestRepository implements MultiSelectListRepository {

    List<MultiSelectItem> myList;
    public MultiSelectTestRepository() {
        myList = new ArrayList<>();
        myList.add(new MultiSelectItem("abortion", "Abortion", "\\{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"\\}", "", "", ""));
        myList.add(new MultiSelectItem("arucellosis", "arucellosis", "\\{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"\\}", "", "", ""));
        myList.add(new MultiSelectItem("bam", "Bacteria Menigitis", "\\{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"\\}", "", "", ""));
        myList.add(new MultiSelectItem("baccess", "BAbcess", "\\{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"\\}", "", "", ""));
    }

    @Override
    public List<MultiSelectItem> fetchData() {
        return myList;
    }
}
