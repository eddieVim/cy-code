package cn.cy.course.controller;

import cn.cy.course.entity.Result;
import cn.cy.course.pojo.Course;
import cn.cy.course.pojo.Pack;
import cn.cy.course.pojo.Selection;
import cn.cy.course.service.SeckillSelectionService;
import cn.cy.course.service.SelectionService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author eddieVim
 * @微信公众号 埃迪的Code日记 / PositiveEddie
 * @blog https://blog.csdn.net/weixin_44129784
 * @create 2020/9/17 10:33 下午
 */
@RestController
@RequestMapping("/seckill/selection")
public class SeckillSelectionController {

    @Reference
    private SeckillSelectionService seckillSelectionService;

    @Reference
    private SelectionService selectionService;

    /**
     * 添加选课请求
     *
     * @param courseId
     * @return
     */
    @GetMapping("/add")
    public Result add(String courseId, String stuId) {
        String studentId = stuId;
        //1. 通过Sercurity获取学号
        // String studentId = "201841054085";

        //2. 课程选课包
        Pack pack = new Pack();
        pack.setStudentId(studentId);
        pack.setCourseId(courseId);

        //3. 处理Pack任务
        seckillSelectionService.add(pack);

        return new Result(0, "选课排队中！");
    }

    /**
     * 查询历史选课情况
     *
     * @return
     */
    @GetMapping("/query/history")
    public List<Course> queryHistory() {
        //1. 通过Sercurity获取学号
        String studentId = "201841054085";

        //2. 获取对应的选课信息
        Map<String, Object> map = new HashMap<>();
        map.put("studentId", studentId);
        List<Course> res = selectionService.historySelection(studentId);
        //3. 根据学期排序；同学期，根据学分排序
        Collections.sort(res, new Comparator<Course>() {
            @Override
            public int compare(Course o1, Course o2) {
                if (o1.getTerm() == null ||
                        o2.getTerm() == null ||
                        o1.getTerm().equals(o2.getTerm())
                ) {
                    return o2.getCredit() - o1.getCredit();
                }
                return o2.getTerm() - o1.getTerm();
            }
        });

        return res;
    }

    /**
     *
     *
     * @return
     */
    @GetMapping("/query/currTerm")
    public List<Course> queryCurrTerm() {
        //1. 通过Sercurity获取学号
        String studentId = "201841054085";

        //2. 获取对应的选课信息
        List<Course> res = selectionService.currTermSelection(studentId);


        //3. 根据学分排序
        res.sort(new Comparator<Course>() {
            @Override
            public int compare(Course o1, Course o2) {
                if (o1.getCredit() == null || o2.getCredit() == null) {
                    return -1;
                }
                return o2.getCredit() - o1.getCredit();
            }
        });

        return res;
    }
}
