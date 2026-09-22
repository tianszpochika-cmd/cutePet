/**
 * 质量覆盖映射（T11.1）：关键规则域 → 已落地测试文件与断言标记。
 * 用途：coverage.test.ts 静态核对 + test-coverage-report.mjs 生成覆盖率证据。
 * 说明：无插桩工具（本机零安装约束），以「规则域→测试文件→断言标记存在性与数量」作为覆盖率口径。
 */

export interface Area {
  id: string;
  label: string;
  requiredBy: string[]; // 任务/验收引用
  testFiles: string[]; // 相对仓库根
  markers: string[]; // 文件内容必须出现的断言标记（≥1 个/每个标记可选）
  minMarkers: number;
}

/** 核心 4 状态机 + 权限点 + 防刷/波动/排期（T11.1 硬要求） */
export const CORE_AREAS: Area[] = [
  {
    id: 'state-auth',
    label: '状态机①账号/会话（注册-登录-注销冷静期）',
    requiredBy: ['T11.1'],
    testFiles: ['services/iam-service/src/test/java/com/cutepet/iam/IamLogicTest.java'],
    markers: ['冷静期', 'ageBand', 'WECHAT'],
    minMarkers: 2,
  },
  {
    id: 'state-submission',
    label: '状态机②内容投稿（草稿→待审→在审→发布/驳回/撤回）',
    requiredBy: ['T11.1'],
    testFiles: ['packages/contracts/test/domain.test.ts', 'apps/web/src/domain/submission.test.ts'],
    markers: ['WITHDRAWN', 'REJECTED', 'TAKEDOWN'],
    minMarkers: 2,
  },
  {
    id: 'state-pet',
    label: '状态机③宠物与提醒（ACTIVE/ARCHIVED/DELETED + 提醒四态）',
    requiredBy: ['T11.1'],
    testFiles: ['services/pet-service/src/test/java/com/cutepet/pet/PetLogicTest.java', 'apps/web/src/domain/pet.test.ts'],
    markers: ['ARCHIVED', 'DELETED', 'EXPIRED'],
    minMarkers: 2,
  },
  {
    id: 'state-product',
    label: '状态机④商品（上架/下架/治理清除 + 引用降级）',
    requiredBy: ['T11.1'],
    testFiles: ['services/catalog-service/src/test/java/com/cutepet/catalog/CatalogLogicTest.java'],
    markers: ['CLEARED', 'OFF_SHELF', 'U88'],
    minMarkers: 2,
  },
  {
    id: 'permission-rbac',
    label: '权限点（32 权限 + 6 预置角色矩阵）',
    requiredBy: ['T11.1'],
    testFiles: [
      'services/admin-ops-service/src/test/java/com/cutepet/adminops/RbacLogicTest.java',
      'apps/admin/src/domain/admin.test.ts',
      'packages/contracts/test/permissions.test.ts',
    ],
    markers: ['supervisor', 'administrator', '32'],
    minMarkers: 2,
  },
  {
    id: 'anti-abuse',
    label: '防刷（评价/报名/关注流速 + CSV 三通道）',
    requiredBy: ['T11.1', 'T11.6'],
    testFiles: [
      'services/explore-service/src/test/java/com/cutepet/explore/ExploreLogicTest.java',
      'apps/web/src/domain/explore.test.ts',
      'apps/admin/src/domain/admin.test.ts',
    ],
    markers: ['U68', 'suspect', 'CSV'],
    minMarkers: 2,
  },
  {
    id: 'weight-fluctuation',
    label: '体重波动（10% 确认 / 5% 高亮 / 趋势高亮）',
    requiredBy: ['T11.1'],
    testFiles: ['services/pet-service/src/test/java/com/cutepet/pet/PetLogicTest.java', 'apps/web/src/domain/pet.test.ts'],
    markers: ['needsWeightConfirm', 'trendMarked', '10%'],
    minMarkers: 1,
  },
  {
    id: 'schedule',
    label: '排期规则（固定间隔/周期日/基准切换/U55–59）',
    requiredBy: ['T11.1'],
    testFiles: ['services/pet-service/src/test/java/com/cutepet/pet/PetLogicTest.java', 'packages/contracts/test/domain.test.ts'],
    markers: ['nextOccurrence', 'basis', 'U55'],
    minMarkers: 1,
  },
];

/** 全量测试文件清单（T11.1 覆盖率=已挂套件全集） */
export const TEST_SUITE_GLOBS = [
  'packages/**/*.test.ts',
  'apps/**/*.test.ts',
  'scripts/**/*.test.ts',
  'scripts/**/*.test.mjs',
  'services/**/*LogicTest.java',
];
