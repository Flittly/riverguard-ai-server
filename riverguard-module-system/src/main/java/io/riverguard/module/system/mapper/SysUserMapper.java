package io.riverguard.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.riverguard.module.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
