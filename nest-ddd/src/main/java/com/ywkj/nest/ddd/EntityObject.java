package com.ywkj.nest.ddd;

import com.ywkj.nest.core.log.ILog;
import com.ywkj.nest.core.log.LogAdapter;
import com.ywkj.nest.core.utils.SpringUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 实体基类。 当实体与实体之间只存在关联关系时，以实体来标记。 实体在仓储保存时将以引用方式关联，数据库则以主外键关联。
 *
 * @author Jove
 */
public abstract class EntityObject implements Serializable {

    ILog logger = new LogAdapter(this.getClass());
    /**
     * 唯一ID
     */
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected boolean OnVerifiying() {
        return true;
    }

    /**
     * //将自己扮演成一个具体的角色以执行一个操作
     *
     * @param clazz  要扮演的角色的类型
     * @param roleId 如果不为空将通过仓储加载一个数据对象
     * @param <T>
     * @return
     */
    public <T extends AbstractRole> T act(Class<T> clazz, String roleId) {

        T t = null;
        try {
            if (!StringUtils.isEmpty(roleId)) {
                IRoleRepository<T> repository = SpringUtils.getInstance(IRoleRepository.class, clazz.getSimpleName() + "_Repository");
                t = repository.getEntityById(roleId);
            }
            if (t == null)
                t = (T) Class.forName(clazz.getName()).newInstance();
            t.setId(this.getId());
            t.setActor(this);

        } catch (Exception e) {
            logger.fatal(e, clazz, roleId);
        }

        return t;
    }

    public <T extends AbstractRole> Set<T> findRoles(Class<T> clazz) {

        IRoleRepository<T> repository = SpringUtils.getInstance(IRoleRepository.class);
        Set<String> ids = repository.getRoleIds(this.id);
        Set<T> tSet = new HashSet<>();
        for (String id : ids) {
            T t = repository.getEntityById(id);
            tSet.add(t);
        }
        return tSet;
    }

    public void save() {
        SpringUtils.getInstance(AbstractUnitOfWork.class).addEntityObject(this);
    }

    public void remove() {
        SpringUtils.getInstance(AbstractUnitOfWork.class).removeEntityObject(this);
    }
}
