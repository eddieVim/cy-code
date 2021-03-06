import request from '@/utils/request'

export function fetchList() {
  return request({
    url: '/teach/course',
    method: 'get'
  })
}

//根据课程id查询选课学生名单
export function getStudentList(id) {
  return request({
    url: '/teach/getStudentList?id='+id,
    method: 'get'
  })
}


export function fetchArticle(id) {
  return request({
    url: '/vue-element-admin/article/detail',
    method: 'get',
    params: { id }
  })
}

export function fetchPv(pv) {
  return request({
    url: '/vue-element-admin/article/pv',
    method: 'get',
    params: { pv }
  })
}

export function createArticle(data) {
  return request({
    url: '/vue-element-admin/article/create',
    method: 'post',
    data
  })
}

export function updateArticle(data) {
  return request({
    url: '/vue-element-admin/article/update',
    method: 'post',
    data
  })
}
