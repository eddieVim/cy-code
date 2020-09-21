package cn.cy.course.task;

import cn.cy.course.pojo.Course;
import cn.cy.course.pojo.Pack;
import cn.cy.course.pojo.Selection;
import cn.cy.course.service.SelectionService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * @author eddieVim
 * @微信公众号 埃迪的Code日记 / PositiveEddie
 * @blog https://blog.csdn.net/weixin_44129784
 * @create 2020/9/18 5:10 下午
 */
@Component
public class CreateSelectionExcutor {

    @Autowired
    private RedisTemplate redisTemplate;

    @Reference
    private SelectionService selectionService;

    private String SECKILL_QUEUE = "SECKILL_QUEUE";

    private String COURSE_MSG_HASH = "COURSE_MSG_HASH";

    private String COURSE_STOCK_QUEUE = "COURSE_STOCK_QUEUE";

    private String COURSE_STOCK_HASH = "COURSE_STOCK_HASH";

    /**
     * 1. 从redis list中取出处理
     * 2. 处理库存（库存队列，课程信息库存）
     * 2. 将处理好的抢课信息入到MQ延时队列中，等待入库
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void createSelection() {
        System.out.println("-----抢课Pack处理-----");
        // 1. 取出任务
        Pack pack = (Pack) redisTemplate.boundListOps(SECKILL_QUEUE).rightPop();
        Set<String> courseIdSet = pack.getCourseIdSet();

        for (String courseId : courseIdSet) {
            String queueId = (String) redisTemplate.boundListOps(COURSE_STOCK_QUEUE).rightPop();

            if (queueId == null || courseId.equals(queueId)) {
                continue;
            }
            /**
             * 从Course-stock-hash中取出库存数量
             * 借助Redis的单线程原子性操作进行库存自减
             *
             * 防止多线程问题出现数据不一致问题
              */
            Course course = (Course) redisTemplate.boundHashOps(COURSE_MSG_HASH).get(courseId);
            Long count = redisTemplate.boundHashOps(COURSE_STOCK_HASH).increment(courseId, -1);
            course.setCount(count.intValue());
            redisTemplate.boundHashOps(COURSE_MSG_HASH).put(courseId, course);

            // 选课信息入库
            Selection selection = new Selection();
            selection.setStudentId(pack.getStudentId());
            selection.setCourseId(courseId);
            selectionService.add(selection);
        }
    }
}