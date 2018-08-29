package llcweb.com.service.impl;

import llcweb.com.dao.repository.DocumentRepository;
import llcweb.com.domain.entity.UsefulDocument;
import llcweb.com.domain.models.Document;
import llcweb.com.domain.models.Roles;
import llcweb.com.domain.models.Users;
import llcweb.com.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    /**
      * 动态查找
     */
    @Override
    public Page<Document> findAll(UsefulDocument document, int pageNum, int pageSize) {

        //按时间排序
        Pageable pageable=new PageRequest(pageNum,pageSize, Sort.Direction.DESC,"createDate");

        Page<Document> documentList = documentRepository.findAll(new Specification<Document>() {
            @Override
            public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> query, CriteriaBuilder cd) {
                Predicate predicate = cd.conjunction();
                if (document.getAuthor() != null) {
                    predicate.getExpressions().add(cd.like(root.get("author"), "%" + document.getAuthor() + "%"));
                }
                if (document.getFirstDate() != null) {
                    predicate.getExpressions().add(cd.greaterThanOrEqualTo(root.get("createDate"), document.getFirstDate()));
                }
                if (document.getLastDate() != null) {
                    predicate.getExpressions().add(cd.lessThanOrEqualTo(root.get("createDate"), document.getLastDate()));
                }
                if (document.getTitle() != null) {
                    predicate.getExpressions().add(cd.like(root.get("title"), "%" + document.getTitle() + "%"));
                }
                if (document.getModel() != null) {
                    predicate.getExpressions().add(cd.like(root.get("model"), "%" + document.getModel() + "%"));
                }
                return predicate;
            }
        }, pageable);

        return documentList;
    }

    /**
     * 查找用户编辑过的文档
     */
    @Override
    public List<Document> selectAll(Users user) {
        List<Document> documents=new ArrayList<>();
        List<Roles> roles=user.getRoles();
        for(Roles role:roles){
            //管理员查看所有文档
            if(role.getrName().equals("admin")){
                documents=documentRepository.findAll();
            }else{ //普通用户查看本人编辑文档
                documents=documentRepository.findByAuthorId(user.getId());
            }
        }
        return documents;
    }

    /**
     * 添加document
     */
    @Override
    public Map<String,Object> add(Document document) {
        Map<String,Object> map=new HashMap<>();

        if(documentRepository.findOne(document.getId())==null){
            if(documentRepository.save(document)!=null){
                map.put("result",1);
                map.put("msg","文档添加成功！");
                return map;
            }
        }
        map.put("result",0);
        map.put("msg","添加失败，请确认文档是否已存在！");
        return map;
    }

    /**
     * 更新document
     */
    @Override
    public Map<String,Object> update(Document document) {

        Map<String,Object> map=new HashMap<>();

        if(documentRepository.findOne(document.getId())!=null){
            if(documentRepository.save(document)!=null){
                map.put("result",1);
                map.put("msg","文档修改成功！");
                return map;
            }
        }
        map.put("result",0);
        map.put("msg","更新失败，请确认文档是否存在！");
        return map;
    }

    /**
     * 删除document
     */
    @Override
    public Map<String,Object> delete(Document document) {

        Map<String,Object> map=new HashMap<>();

        if(documentRepository.findOne(document.getId())!=null){
            if(documentRepository.save(document)!=null){
                map.put("result",1);
                map.put("msg","文档已删除！");
                return map;
            }
        }
        map.put("result",0);
        map.put("msg","删除失败，请确认文档是否存在！");
        return map;
    }
}
