package com.virjar.dungproxy.server.service.impl;

import com.virjar.dungproxy.server.core.beanmapper.BeanMapper;
import com.virjar.dungproxy.server.entity.DomainMeta;
import com.virjar.dungproxy.server.model.DomainMetaModel;
import com.virjar.dungproxy.server.repository.DomainMetaRepository;
import com.virjar.dungproxy.server.service.DomainMetaService;
import org.joda.time.DateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DomainMetaServiceImpl implements DomainMetaService {
    @Resource
    private BeanMapper beanMapper;

    @Resource
    private DomainMetaRepository domainMetaRepo;

    @Transactional
    @Override
    public int create(DomainMetaModel domainMetaModel) {
        return domainMetaRepo.insert(beanMapper.map(domainMetaModel, DomainMeta.class));
    }

    @Transactional
    @Override
    public int createSelective(DomainMetaModel domainMetaModel) {
        return domainMetaRepo.insertSelective(beanMapper.map(domainMetaModel, DomainMeta.class));
    }

    @Transactional
    @Override
    public int deleteByPrimaryKey(Long id) {
        return domainMetaRepo.deleteByPrimaryKey(id);
    }

    @Transactional(readOnly = true)
    @Override
    public DomainMetaModel findByPrimaryKey(Long id) {
        DomainMeta domainMeta = domainMetaRepo.selectByPrimaryKey(id);
        return beanMapper.map(domainMeta, DomainMetaModel.class);
    }

    @Transactional(readOnly = true)
    @Override
    public int selectCount(DomainMetaModel domainMetaModel) {
        return domainMetaRepo.selectCount(beanMapper.map(domainMetaModel, DomainMeta.class));
    }

    @Transactional
    @Override
    public int updateByPrimaryKey(DomainMetaModel domainMetaModel) {
        return domainMetaRepo.updateByPrimaryKey(beanMapper.map(domainMetaModel, DomainMeta.class));
    }

    @Transactional
    @Override
    public int updateByPrimaryKeySelective(DomainMetaModel domainMetaModel) {
        return domainMetaRepo.updateByPrimaryKeySelective(beanMapper.map(domainMetaModel, DomainMeta.class));
    }

    @Transactional(readOnly = true)
    @Override
    public List<DomainMetaModel> selectPage(DomainMetaModel domainMetaModel, Pageable pageable) {
        if (domainMetaModel == null) {
            domainMetaModel = new DomainMetaModel();
        }
        if (pageable == null) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE);
        }
        List<DomainMeta> domainMetaList = domainMetaRepo.selectPage(beanMapper.map(domainMetaModel, DomainMeta.class),
                pageable);
        return beanMapper.mapAsList(domainMetaList, DomainMetaModel.class);
    }

    @Override
    public List<DomainMetaModel> selectBefore(int days, Pageable pageable) {
        try {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.add(Calendar.DATE, -days);
            Date today = calendar1.getTime();
            java.sql.Date date = new java.sql.Date(today.getTime());
            List<DomainMeta> domainMetas = domainMetaRepo.selectBefore(date, pageable);
            return beanMapper.mapAsList(domainMetas, DomainMetaModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}