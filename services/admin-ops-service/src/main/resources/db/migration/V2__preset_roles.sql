-- cutePet admin-ops-service · V2：预置 6 角色与权限种子（与 RbacEngine/contracts 镜像 · T6.1/U25）
INSERT INTO roles (id, name, builtin, created_at) VALUES
  (1, 'editor', 1, NOW()),
  (2, 'reviewer', 1, NOW()),
  (3, 'poiOperator', 1, NOW()),
  (4, 'productOperator', 1, NOW()),
  (5, 'supervisor', 1, NOW()),
  (6, 'administrator', 1, NOW());

INSERT INTO role_permissions (role_id, permission) VALUES
  (1,'article.create'),(1,'article.edit.any'),(1,'article.pinned.schedule'),
  (1,'taxonomy.manage'),(1,'banner.manage'),(1,'list.manage'),(1,'review.bind.product'),
  (2,'article.takedown'),(2,'review.article'),(2,'comment.manage'),(2,'report.handle'),
  (2,'ugv.review.hide'),(2,'route.approve'),(2,'user.view'),(2,'user.post.right.cancel'),(2,'dashboard.view.all'),
  (3,'poi.create.edit'),(3,'poi.close'),(3,'correction.handle'),(3,'ugv.review.hide'),
  (3,'route.approve'),(3,'activity.manage'),(3,'activity.export'),
  (4,'product.create.edit'),(4,'product.takedown'),(4,'product.import.csv'),
  (4,'list.manage'),(4,'review.bind.product'),
  (5,'article.create'),(5,'article.publish.direct'),(5,'article.edit.any'),(5,'article.takedown'),
  (5,'article.pinned.schedule'),(5,'review.article'),(5,'comment.manage'),(5,'report.handle'),
  (5,'taxonomy.manage'),(5,'banner.manage'),(5,'product.create.edit'),(5,'product.takedown'),
  (5,'product.import.csv'),(5,'list.manage'),(5,'review.bind.product'),(5,'poi.create.edit'),
  (5,'poi.close'),(5,'correction.handle'),(5,'ugv.review.hide'),(5,'route.approve'),
  (5,'activity.manage'),(5,'activity.export'),(5,'user.view'),(5,'user.ban'),
  (5,'user.post.right.cancel'),(5,'family.dispute'),(5,'appeal.handle'),(5,'dashboard.view.all'),
  (5,'notify.template'),(5,'audit.view'),
  (6,'article.create'),(6,'article.publish.direct'),(6,'article.edit.any'),(6,'article.takedown'),
  (6,'article.pinned.schedule'),(6,'review.article'),(6,'comment.manage'),(6,'report.handle'),
  (6,'taxonomy.manage'),(6,'banner.manage'),(6,'product.create.edit'),(6,'product.takedown'),
  (6,'product.import.csv'),(6,'list.manage'),(6,'review.bind.product'),(6,'poi.create.edit'),
  (6,'poi.close'),(6,'correction.handle'),(6,'ugv.review.hide'),(6,'route.approve'),
  (6,'activity.manage'),(6,'activity.export'),(6,'user.view'),(6,'user.ban'),
  (6,'user.post.right.cancel'),(6,'family.dispute'),(6,'appeal.handle'),(6,'dashboard.view.all'),
  (6,'notify.template'),(6,'audit.view'),(6,'rbac.manage'),(6,'system.settings');
