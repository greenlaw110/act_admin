package com.act.admin.services.authority;

import com.act.admin.constraints.authority.AuthorityConsts;
import com.act.admin.forms.authority.ResourceAddForm;
import com.act.admin.forms.authority.ResourceSearchForm;
import com.act.admin.models.authority.AdminResourcesModel;
import com.act.admin.services.BaseService;
import io.ebean.Ebean;
import io.ebean.Expr;
import io.ebean.Junction;
import io.ebean.PagedList;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.S;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: shaofangjie
 * Date: 2018-10-31
 * Time: 10:09 PM
 */
public class AuthorityResourcesService extends BaseService implements AuthorityConsts {

    private static Logger logger = L.get(AuthorityResourcesService.class);

    public PagedList<AdminResourcesModel> getAdminResourcePageList(final ResourceSearchForm resourceSearchForm, final int page, final int limit) {

        try {
            Ebean.beginTransaction();

            Junction<AdminResourcesModel> adminResourcesModelJunction = AdminResourcesModel.find.query().where().conjunction();

            if (S.isNotBlank(resourceSearchForm.getResourceName())) {
                adminResourcesModelJunction.add(Expr.eq("sourceName", resourceSearchForm.getResourceName()));
            }
            //排序
            if ("asc".equals(resourceSearchForm.getOrderDir())) {
                adminResourcesModelJunction.order().asc(resourceSearchForm.getOrderColumn());
            } else {
                adminResourcesModelJunction.order().desc(resourceSearchForm.getOrderColumn());
            }


            //分页参数
            adminResourcesModelJunction.setFirstRow((page - 1) * limit);
            adminResourcesModelJunction.setMaxRows(limit);

            PagedList<AdminResourcesModel> pagedList = adminResourcesModelJunction.findPagedList();
            pagedList.loadCount();

            Ebean.commitTransaction();

            return pagedList;

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("查询后台资源列表出现错误: %s" + ex.getMessage());
            Ebean.rollbackTransaction();
        } finally {
            Ebean.endTransaction();
        }

        return null;

    }

    public AdminResourceAddResult adminResourceSave(ResourceAddForm resourceAddForm) {
        try {
            Ebean.beginTransaction();

            AdminResourcesModel parentResource = null;
            AdminResourcesModel newResource = new AdminResourcesModel();

            if ("0".equals(resourceAddForm.getResourcePid())) {
                newResource.sourcePid = parentResource;
            } else {
                parentResource = AdminResourcesModel.find.byId(Long.parseLong(resourceAddForm.getResourcePid()));
                if (null == parentResource) {
                    return AdminResourceAddResult.PARENT_IS_NULL;
                }
            }

            newResource.sourcePid = parentResource;
            newResource.sourceType = Integer.parseInt(resourceAddForm.getResourceType());
            newResource.enabled = null != resourceAddForm.getEnable() && "1".equals(resourceAddForm.getEnable());
            newResource.sourceName = resourceAddForm.getResourceName();
            newResource.sourceUrl = resourceAddForm.getResourceUrl();
            newResource.sourceFunction = resourceAddForm.getResourceFun();
            newResource.sourceOrder = Integer.parseInt(resourceAddForm.getResourceOrder());
            newResource.iconfont = resourceAddForm.getIconfont();
            newResource.save();

            Ebean.commitTransaction();
            return AdminResourceAddResult.ADD_SUCCESS;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("保存后台资源出现错误: %s" + ex.getMessage());
            Ebean.rollbackTransaction();
            return AdminResourceAddResult.ADD_FAILED;
        } finally {
            Ebean.endTransaction();
        }

    }

    public List<Map<String, String>> getAllParentResources() {

        try {
            Ebean.beginTransaction();

            List<AdminResourcesModel> allResourcesList = AdminResourcesModel.find.query().fetchLazy("sourcePid").findList();

            List<Map<String, String>> allParentResources = new ArrayList<>();
            Map<String, String> map = new HashMap<>();
            map.put("id", "0");
            map.put("name", "顶级资源");
            allParentResources.add(map);
            for (AdminResourcesModel topResource : allResourcesList) {
                if (null == topResource.sourcePid) {
                    Map<String, String> topMap = new HashMap<>();
                    topMap.put("id", topResource.getId().toString());
                    topMap.put("name", "┝ " + topResource.sourceName);
                    allParentResources.add(topMap);
                    for (AdminResourcesModel secondResource : allResourcesList) {
                        if (0 == secondResource.sourceType && null != secondResource.sourcePid && secondResource.sourcePid.getId().equals(topResource.getId())) {
                            Map<String, String> secondMap = new HashMap<>();
                            secondMap.put("id", secondResource.getId().toString());
                            secondMap.put("name", "&nbsp;&nbsp;┝ " + secondResource.sourceName);
                            allParentResources.add(secondMap);
                            for (AdminResourcesModel threeResource : allResourcesList) {
                                if (0 == threeResource.sourceType && null != threeResource.sourcePid && threeResource.sourcePid.getId().equals(secondResource.getId())) {
                                    Map<String, String> threeMap = new HashMap<>();
                                    threeMap.put("id", threeResource.getId().toString());
                                    threeMap.put("name", "&nbsp;&nbsp;&nbsp;&nbsp;┝ " + threeResource.sourceName);
                                    allParentResources.add(threeMap);
                                }
                            }
                        }
                    }
                }
            }

            Ebean.commitTransaction();

            return allParentResources;

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("查询后台资源列表出现错误: %s" + ex.getMessage());
            Ebean.rollbackTransaction();
            return null;
        } finally {
            Ebean.endTransaction();
        }

    }

}
