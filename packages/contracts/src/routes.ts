import type { Permission } from './permissions';

/** 路由契约（OpenAPI 前身）：契约先行 v1 —— 前端 client 与服务端路由均以此生成/校验。 */
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
export type RouteAuth = 'public' | 'user' | 'admin';

export interface RouteSpec {
  /** camelCase 唯一 id，作为 api client 函数键 */
  id: string;
  method: HttpMethod;
  path: string; // 支持 :param
  auth: RouteAuth;
  /** 仅 admin 路由必填，且必须是 32 权限点之一 */
  permission?: Permission;
  summary: string;
}

export const ROUTES: RouteSpec[] = [
  // ---- 认证与个人（M1） ----
  { id: 'authSmsSend', method: 'POST', path: '/auth/sms/send', auth: 'public', summary: '发送验证码' },
  { id: 'authSmsLogin', method: 'POST', path: '/auth/sms/login', auth: 'public', summary: '手机号登录' },
  { id: 'authWechatLogin', method: 'POST', path: '/auth/wechat/login', auth: 'public', summary: '微信登录' },
  { id: 'authRefresh', method: 'POST', path: '/auth/refresh', auth: 'public', summary: '刷新令牌' },
  { id: 'authLogout', method: 'POST', path: '/auth/logout', auth: 'user', summary: '登出' },
  { id: 'minorGuardianVerify', method: 'POST', path: '/auth/minor/guardian/verify', auth: 'public', summary: '监护人验证' },
  { id: 'minorGuardianConsent', method: 'POST', path: '/auth/minor/guardian/consent', auth: 'public', summary: '监护人同意' },
  { id: 'minorGuardianRevoke', method: 'POST', path: '/auth/minor/guardian/revoke', auth: 'user', summary: '监护人撤回' },
  { id: 'meGet', method: 'GET', path: '/me', auth: 'user', summary: '个人资料' },
  { id: 'meUpdate', method: 'PATCH', path: '/me', auth: 'user', summary: '修改资料' },
  { id: 'mePhoneBind', method: 'POST', path: '/me/phone/bind', auth: 'user', summary: '换绑手机' },
  { id: 'meExport', method: 'POST', path: '/me/export', auth: 'user', summary: '数据导出' },
  { id: 'meDelete', method: 'POST', path: '/me/delete', auth: 'user', summary: '注销申请' },
  { id: 'meCancelDelete', method: 'POST', path: '/me/cancel-delete', auth: 'user', summary: '撤销注销' },
  { id: 'track', method: 'POST', path: '/track', auth: 'public', summary: '埋点' },

  // ---- 家庭共享（M1/A5/T1.9） ----
  { id: 'familyCreate', method: 'POST', path: '/families', auth: 'user', summary: '创建家庭' },
  { id: 'familyMine', method: 'GET', path: '/families/mine', auth: 'user', summary: '我的家庭' },
  { id: 'familyInviteCreate', method: 'POST', path: '/families/invite', auth: 'user', summary: '生成邀请' },
  { id: 'familyJoin', method: 'POST', path: '/families/join', auth: 'user', summary: '加入家庭' },
  { id: 'familyLeave', method: 'POST', path: '/families/leave', auth: 'user', summary: '退出家庭' },
  { id: 'familyMemberRemove', method: 'DELETE', path: '/families/members/:userId', auth: 'user', summary: '移除成员' },
  { id: 'familyDissolve', method: 'POST', path: '/families/dissolve', auth: 'user', summary: '解散家庭' },
  { id: 'familyTransferOwner', method: 'POST', path: '/families/transfer-owner', auth: 'user', summary: '所有权转移' },
  { id: 'familyTransferAdmin', method: 'POST', path: '/families/transfer-admin', auth: 'user', summary: '管理员转交' },
  { id: 'familyTransferRespond', method: 'PUT', path: '/families/transfers/respond', auth: 'user', summary: '转移响应' },
  { id: 'familySharePet', method: 'PATCH', path: '/families/share/:petId', auth: 'user', summary: '共享档位' },

  // ---- 宠物管理（M2） ----
  { id: 'petsList', method: 'GET', path: '/pets', auth: 'user', summary: '宠物列表' },
  { id: 'petCreate', method: 'POST', path: '/pets', auth: 'user', summary: '创建宠物' },
  { id: 'petGet', method: 'GET', path: '/pets/:id', auth: 'user', summary: '宠物详情' },
  { id: 'petUpdate', method: 'PATCH', path: '/pets/:id', auth: 'user', summary: '编辑档案' },
  { id: 'petArchive', method: 'POST', path: '/pets/:id/archive', auth: 'user', summary: '归档' },
  { id: 'petDelete', method: 'DELETE', path: '/pets/:id', auth: 'user', summary: '软删30天' },
  { id: 'petRestore', method: 'POST', path: '/pets/:id/restore', auth: 'user', summary: '恢复' },
  { id: 'petTimeline', method: 'GET', path: '/pets/:id/timeline', auth: 'user', summary: '时间线' },
  { id: 'petRecords', method: 'GET', path: '/pets/:id/records', auth: 'user', summary: '健康记录' },
  { id: 'recordCreate', method: 'POST', path: '/pets/:id/records', auth: 'user', summary: '新增记录' },
  { id: 'recordUpdate', method: 'PATCH', path: '/records/:id', auth: 'user', summary: '编辑记录' },
  { id: 'recordDelete', method: 'DELETE', path: '/records/:id', auth: 'user', summary: '删除记录' },
  { id: 'petWeights', method: 'GET', path: '/pets/:id/weights', auth: 'user', summary: '体重曲线' },
  { id: 'petSummaryPreview', method: 'POST', path: '/pets/:id/summary/preview', auth: 'user', summary: '摘要预览' },
  { id: 'petSummaryGet', method: 'GET', path: '/pets/:id/summary', auth: 'user', summary: '摘要获取' },

  // ---- 提醒与计划待办（M2/T2.6） ----
  { id: 'reminderList', method: 'GET', path: '/pets/:id/reminders', auth: 'user', summary: '提醒列表' },
  { id: 'reminderCreate', method: 'POST', path: '/pets/:id/reminders', auth: 'user', summary: '创建提醒' },
  { id: 'reminderUpdate', method: 'PATCH', path: '/reminders/:id', auth: 'user', summary: '编辑提醒' },
  { id: 'reminderComplete', method: 'POST', path: '/reminders/:id/complete', auth: 'user', summary: '完成提醒' },
  { id: 'reminderSkip', method: 'POST', path: '/reminders/:id/skip', auth: 'user', summary: '跳过(记原因)' },
  { id: 'reminderDelete', method: 'DELETE', path: '/reminders/:id', auth: 'user', summary: '关闭提醒' },
  { id: 'remindersPending', method: 'GET', path: '/reminders/pending', auth: 'user', summary: '待处理' },
  { id: 'planList', method: 'GET', path: '/pets/:id/plans', auth: 'user', summary: '计划列表' },
  { id: 'planCreate', method: 'POST', path: '/pets/:id/plans', auth: 'user', summary: '创建计划' },
  { id: 'planUpdate', method: 'PATCH', path: '/plans/:id', auth: 'user', summary: '编辑计划' },
  { id: 'planTodoComplete', method: 'POST', path: '/plan-todos/:id/complete', auth: 'user', summary: '完成待办' },

  // ---- 资讯（M3） ----
  { id: 'articlesFeed', method: 'GET', path: '/articles', auth: 'public', summary: '文章feed' },
  { id: 'articleGet', method: 'GET', path: '/articles/:slug', auth: 'public', summary: '文章详情' },
  { id: 'channelsList', method: 'GET', path: '/channels', auth: 'public', summary: '频道' },
  { id: 'searchGet', method: 'GET', path: '/search', auth: 'public', summary: '站内搜索' },
  { id: 'topicsList', method: 'GET', path: '/topics', auth: 'public', summary: '话题' },
  { id: 'specialGet', method: 'GET', path: '/specials/:id', auth: 'public', summary: '专题' },
  { id: 'articleLike', method: 'POST', path: '/articles/:id/like', auth: 'user', summary: '点赞' },
  { id: 'articleUnlike', method: 'DELETE', path: '/articles/:id/like', auth: 'user', summary: '取消赞' },
  { id: 'articleFavorite', method: 'POST', path: '/articles/:id/favorite', auth: 'user', summary: '收藏' },
  { id: 'articleUnfavorite', method: 'DELETE', path: '/articles/:id/favorite', auth: 'user', summary: '取消藏' },
  { id: 'commentCreate', method: 'POST', path: '/articles/:id/comments', auth: 'user', summary: '评论' },
  { id: 'commentsList', method: 'GET', path: '/articles/:id/comments', auth: 'public', summary: '评论列表' },
  { id: 'commentLike', method: 'POST', path: '/comments/:id/like', auth: 'user', summary: '评论点赞' },
  { id: 'favoritesList', method: 'GET', path: '/me/favorites', auth: 'user', summary: '我的收藏' },

  // ---- 投稿与作者（M3/T3.6） ----
  { id: 'submissionList', method: 'GET', path: '/me/submissions', auth: 'user', summary: '我的投稿' },
  { id: 'submissionCreate', method: 'POST', path: '/me/submissions', auth: 'user', summary: '创建投稿' },
  { id: 'submissionUpdate', method: 'PATCH', path: '/me/submissions/:id', auth: 'user', summary: '编辑草稿' },
  { id: 'submissionSubmit', method: 'POST', path: '/me/submissions/:id/submit', auth: 'user', summary: '提交审核' },
  { id: 'submissionWithdraw', method: 'POST', path: '/me/submissions/:id/withdraw', auth: 'user', summary: '撤回' },
  { id: 'authorGet', method: 'GET', path: '/authors/:id', auth: 'public', summary: '作者主页' },
  { id: 'followCreate', method: 'POST', path: '/authors/:id/follow', auth: 'user', summary: '关注' },
  { id: 'followDelete', method: 'DELETE', path: '/authors/:id/follow', auth: 'user', summary: '取关' },

  // ---- 探索（M4/T4.6） ----
  { id: 'poisSearch', method: 'GET', path: '/pois', auth: 'public', summary: 'POI检索' },
  { id: 'poiGet', method: 'GET', path: '/pois/:id', auth: 'public', summary: 'POI详情' },
  { id: 'poiReviewCreate', method: 'POST', path: '/pois/:id/reviews', auth: 'user', summary: '写评价' },
  { id: 'poiReviewsList', method: 'GET', path: '/pois/:id/reviews', auth: 'public', summary: '评价列表' },
  { id: 'poiCorrectionCreate', method: 'POST', path: '/pois/:id/corrections', auth: 'user', summary: '纠错' },
  { id: 'poiNavClick', method: 'POST', path: '/pois/:id/nav-click', auth: 'public', summary: '导航计数' },
  { id: 'poiFavorite', method: 'POST', path: '/pois/:id/favorite', auth: 'user', summary: '收藏场所' },
  { id: 'poiUnfavorite', method: 'DELETE', path: '/pois/:id/favorite', auth: 'user', summary: '取消收藏' },
  { id: 'routesList', method: 'GET', path: '/routes', auth: 'public', summary: '路线列表' },
  { id: 'routeCreate', method: 'POST', path: '/routes', auth: 'user', summary: '发布路线' },
  { id: 'routeGet', method: 'GET', path: '/routes/:id', auth: 'public', summary: '路线详情' },
  { id: 'routeLike', method: 'POST', path: '/routes/:id/like', auth: 'user', summary: '路线点赞' },
  { id: 'routeFavorite', method: 'POST', path: '/routes/:id/favorite', auth: 'user', summary: '路线收藏' },
  { id: 'activitiesList', method: 'GET', path: '/activities', auth: 'public', summary: '活动列表' },
  { id: 'activityGet', method: 'GET', path: '/activities/:id', auth: 'public', summary: '活动详情' },
  { id: 'activitySignup', method: 'POST', path: '/activities/:id/signup', auth: 'user', summary: '报名(含同意)' },
  { id: 'activitySignupCancel', method: 'POST', path: '/activities/:id/signup/cancel', auth: 'user', summary: '取消报名' },
  { id: 'adoptionsList', method: 'GET', path: '/adoptions', auth: 'public', summary: '领养信息' },

  // ---- 用品（M5/T5.3） ----
  { id: 'productsList', method: 'GET', path: '/products', auth: 'public', summary: '商品列表' },
  { id: 'productGet', method: 'GET', path: '/products/:id', auth: 'public', summary: '商品详情' },
  { id: 'productFavorite', method: 'POST', path: '/products/:id/favorite', auth: 'user', summary: '收藏商品' },
  { id: 'productUnfavorite', method: 'DELETE', path: '/products/:id/favorite', auth: 'user', summary: '取消收藏' },
  { id: 'listsList', method: 'GET', path: '/lists', auth: 'public', summary: '清单列表' },
  { id: 'listGet', method: 'GET', path: '/lists/:id', auth: 'public', summary: '清单详情' },
  { id: 'listLike', method: 'POST', path: '/lists/:id/like', auth: 'user', summary: '清单点赞' },

  // ---- 消息/工单/举报（M1/M6） ----
  { id: 'messagesList', method: 'GET', path: '/messages', auth: 'user', summary: '消息中心' },
  { id: 'messagesReadAll', method: 'POST', path: '/messages/read-all', auth: 'user', summary: '全部已读' },
  { id: 'messageRead', method: 'POST', path: '/messages/:id/read', auth: 'user', summary: '单条已读' },
  { id: 'reportsCreate', method: 'POST', path: '/reports', auth: 'user', summary: '举报' },
  { id: 'myReportsList', method: 'GET', path: '/me/reports', auth: 'user', summary: '我的举报' },
  { id: 'ticketsCreate', method: 'POST', path: '/tickets', auth: 'user', summary: '客服/申诉' },
  { id: 'myTicketsList', method: 'GET', path: '/me/tickets', auth: 'user', summary: '我的工单' },
  { id: 'counterNoticeCreate', method: 'POST', path: '/counters', auth: 'user', summary: '版权反通知' },

  // ---- 管理端（M6/M8 · 全部 admin + 权限点） ----
  { id: 'adminSubmissionsList', method: 'GET', path: '/admin/submissions', auth: 'admin', permission: 'review.article', summary: '投稿队列' },
  { id: 'adminSubmissionClaim', method: 'POST', path: '/admin/submissions/:id/claim', auth: 'admin', permission: 'review.article', summary: '领取' },
  { id: 'adminSubmissionApprove', method: 'POST', path: '/admin/submissions/:id/approve', auth: 'admin', permission: 'review.article', summary: '通过' },
  { id: 'adminSubmissionReject', method: 'POST', path: '/admin/submissions/:id/reject', auth: 'admin', permission: 'review.article', summary: '驳回' },
  { id: 'adminArticleCreate', method: 'POST', path: '/admin/articles', auth: 'admin', permission: 'article.create', summary: '发文' },
  { id: 'adminArticlePublish', method: 'POST', path: '/admin/articles/:id/publish', auth: 'admin', permission: 'article.publish.direct', summary: '免审直发' },
  { id: 'adminArticleUpdate', method: 'PATCH', path: '/admin/articles/:id', auth: 'admin', permission: 'article.edit.any', summary: '编辑任意' },
  { id: 'adminArticleTakedown', method: 'POST', path: '/admin/articles/:id/takedown', auth: 'admin', permission: 'article.takedown', summary: '下架' },
  { id: 'adminArticlePin', method: 'POST', path: '/admin/articles/:id/pin', auth: 'admin', permission: 'article.pinned.schedule', summary: '置顶定时' },
  { id: 'adminCommentDelete', method: 'POST', path: '/admin/comments/:id/delete', auth: 'admin', permission: 'comment.manage', summary: '删评论' },
  { id: 'adminTaxonomyUpdate', method: 'POST', path: '/admin/taxonomy', auth: 'admin', permission: 'taxonomy.manage', summary: '分类标签' },
  { id: 'adminBannerUpsert', method: 'POST', path: '/admin/banners', auth: 'admin', permission: 'banner.manage', summary: '推荐位' },
  { id: 'adminProductCreate', method: 'POST', path: '/admin/products', auth: 'admin', permission: 'product.create.edit', summary: '建商品' },
  { id: 'adminProductUpdate', method: 'PATCH', path: '/admin/products/:id', auth: 'admin', permission: 'product.create.edit', summary: '改商品' },
  { id: 'adminProductImport', method: 'POST', path: '/admin/products/import', auth: 'admin', permission: 'product.import.csv', summary: 'CSV导入' },
  { id: 'adminProductTakedown', method: 'POST', path: '/admin/products/:id/takedown', auth: 'admin', permission: 'product.takedown', summary: '商品下架' },
  { id: 'adminProductBind', method: 'POST', path: '/admin/products/:id/bind', auth: 'admin', permission: 'review.bind.product', summary: '评测关联' },
  { id: 'adminListCreate', method: 'POST', path: '/admin/lists', auth: 'admin', permission: 'list.manage', summary: '运营清单' },
  { id: 'adminPoiCreate', method: 'POST', path: '/admin/pois', auth: 'admin', permission: 'poi.create.edit', summary: '建POI' },
  { id: 'adminPoiUpdate', method: 'PATCH', path: '/admin/pois/:id', auth: 'admin', permission: 'poi.create.edit', summary: '改POI' },
  { id: 'adminPoiClose', method: 'POST', path: '/admin/pois/:id/close', auth: 'admin', permission: 'poi.close', summary: '停业标记' },
  { id: 'adminCorrectionsList', method: 'GET', path: '/admin/corrections', auth: 'admin', permission: 'correction.handle', summary: '纠错队列' },
  { id: 'adminCorrectionResolve', method: 'POST', path: '/admin/corrections/:id/resolve', auth: 'admin', permission: 'correction.handle', summary: '处理纠错' },
  { id: 'adminReviewHide', method: 'POST', path: '/admin/reviews/:id/hide', auth: 'admin', permission: 'ugv.review.hide', summary: '隐藏评价' },
  { id: 'adminRouteReview', method: 'POST', path: '/admin/routes/:id/review', auth: 'admin', permission: 'route.approve', summary: '路线审核' },
  { id: 'adminActivityReview', method: 'POST', path: '/admin/activities/:id/review', auth: 'admin', permission: 'activity.manage', summary: '活动审核' },
  { id: 'adminActivitySignupsExport', method: 'GET', path: '/admin/activities/:id/signups/export', auth: 'admin', permission: 'activity.export', summary: '名单导出' },
  { id: 'adminReportsList', method: 'GET', path: '/admin/reports', auth: 'admin', permission: 'report.handle', summary: '举报队列' },
  { id: 'adminReportResolve', method: 'POST', path: '/admin/reports/:id/resolve', auth: 'admin', permission: 'report.handle', summary: '处理举报' },
  { id: 'adminCopyrightList', method: 'GET', path: '/admin/copyrights', auth: 'admin', permission: 'report.handle', summary: '版权工单' },
  { id: 'adminCopyrightTakedown', method: 'POST', path: '/admin/copyrights/:id/takedown', auth: 'admin', permission: 'report.handle', summary: '通知下架' },
  { id: 'adminCopyrightCounter', method: 'POST', path: '/admin/copyrights/:id/counter', auth: 'admin', permission: 'report.handle', summary: '反通知处理' },
  { id: 'adminAppealsList', method: 'GET', path: '/admin/appeals', auth: 'admin', permission: 'appeal.handle', summary: '申诉队列' },
  { id: 'adminAppealResolve', method: 'POST', path: '/admin/appeals/:id/resolve', auth: 'admin', permission: 'appeal.handle', summary: '处理申诉' },
  { id: 'adminUsersList', method: 'GET', path: '/admin/users', auth: 'admin', permission: 'user.view', summary: '用户查询' },
  { id: 'adminUserBan', method: 'POST', path: '/admin/users/:id/ban', auth: 'admin', permission: 'user.ban', summary: '封禁' },
  { id: 'adminUserUnban', method: 'POST', path: '/admin/users/:id/unban', auth: 'admin', permission: 'user.ban', summary: '解封' },
  { id: 'adminUserPostRight', method: 'POST', path: '/admin/users/:id/post-right', auth: 'admin', permission: 'user.post.right.cancel', summary: '投稿权' },
  { id: 'adminFamilyDisputeResolve', method: 'POST', path: '/admin/families/disputes/:id/resolve', auth: 'admin', permission: 'family.dispute', summary: '家庭纠纷' },
  { id: 'adminRolesList', method: 'GET', path: '/admin/roles', auth: 'admin', permission: 'rbac.manage', summary: '角色列表' },
  { id: 'adminRoleCreate', method: 'POST', path: '/admin/roles', auth: 'admin', permission: 'rbac.manage', summary: '建角色' },
  { id: 'adminRoleUpdate', method: 'PATCH', path: '/admin/roles/:id', auth: 'admin', permission: 'rbac.manage', summary: '改角色' },
  { id: 'adminDashboard', method: 'GET', path: '/admin/dashboard', auth: 'admin', permission: 'dashboard.view.all', summary: '看板' },
  { id: 'adminAuditLogs', method: 'GET', path: '/admin/audit-logs', auth: 'admin', permission: 'audit.view', summary: '审计日志' },
  { id: 'adminNotifyTemplateUpsert', method: 'POST', path: '/admin/notify/templates', auth: 'admin', permission: 'notify.template', summary: '通知模板' },
  { id: 'adminSystemSettings', method: 'POST', path: '/admin/system/settings', auth: 'admin', permission: 'system.settings', summary: '系统设置' },
];
