package com.wcy.rhapsody.admin.service.api.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcy.rhapsody.admin.config.redis.RedisService;
import com.wcy.rhapsody.admin.mapper.api.UserMapper;
import com.wcy.rhapsody.admin.modules.dto.RegisterDTO;
import com.wcy.rhapsody.admin.modules.entity.web.Follow;
import com.wcy.rhapsody.admin.modules.entity.web.Topic;
import com.wcy.rhapsody.admin.modules.entity.web.User;
import com.wcy.rhapsody.admin.modules.vo.ProfileVO;
import com.wcy.rhapsody.admin.service.api.FollowService;
import com.wcy.rhapsody.admin.service.api.TopicService;
import com.wcy.rhapsody.admin.service.api.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 用户 实现类
 *
 * @author Yeeep 2020/11/7
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private FollowService followService;

    @Resource
    private TopicService topicService;

    @Override
    public int createUser(RegisterDTO dto) {
        User user = User.builder()
                .username(dto.getName())
                .alias(dto.getName())
                .password(BCrypt.hashpw(dto.getPass(), BCrypt.gensalt()))
                .email(dto.getEmail())
                .createTime(new Date())
                .build();

        int insert = this.baseMapper.insert(user);

        // TODO: 2020/11/25 暂时先不发送邮件激活了
        // String activeUrl = URLUtil.normalize("127.0.0.1:9999/user?active=123412");
        // // 发送激活邮件
        // String content = "请在30分钟内激活您的账号，如非本人操作，请忽略 </br > " +
        //         "<a href=\"" + activeUrl + "\" target =\"_blank\" '>点击激活账号</a>";
        // MailUtil.send(dto.getEmail(), "R社区账号激活", content, true);
        return insert;
    }

    @Override
    public User selectByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.baseMapper.selectOne(wrapper);
    }

    @Override
    public ProfileVO getUserProfile(String id) {
        ProfileVO profile = new ProfileVO();
        User user = this.baseMapper.selectById(id);
        BeanUtils.copyProperties(user, profile);
        // 用户文章数
        int count = topicService.count(new LambdaQueryWrapper<Topic>().eq(Topic::getUserId, id));
        profile.setTopicCount(count);

        // 粉丝数
        int followers = followService.count(new LambdaQueryWrapper<Follow>().eq(Follow::getParentId, id));
        profile.setFollowerCount(followers);
        // 关注数
        int follows = followService.count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, id));
        profile.setFollowCount(follows);

        return profile;
    }
}
