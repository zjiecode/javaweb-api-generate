package ${package}.base;

import java.util.List;

/**
 * 分页的数据结构
 */
public class Pagination<T> {
    private Integer total;
    private Integer pageIndex;
    private List<T> records;

    public Pagination(Integer total, Integer pageIndex, List<T> records) {
        this.total = total;
        this.pageIndex = pageIndex;
        this.records = records;
    }

    public Integer getTotal() {
        return total;
    }

    public Pagination setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public Pagination setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
        return this;
    }

    public List<T> getRecords() {
        return records;
    }

    public Pagination setRecords(List<T> records) {
        this.records = records;
        return this;
    }
}
