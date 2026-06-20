package io.github.haoyongqiang1999.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommonService {
    public void testForALS(int  count) {
        List<String[]> list = new ArrayList<>();
        while (true) {
            list.add(new String[count]);
        }
    }
}
