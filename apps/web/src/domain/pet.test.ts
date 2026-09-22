import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  WIZARD_STEPS,
  validateWizardStep,
  wizardComplete,
  recordFields,
  validateRecord,
  buildRecurrenceRule,
  parseRecurrenceRule,
  weightSheetConfirm,
  trendHighlight,
  summaryPreview,
  deleteConfirmCopy,
} from '../domain/pet.ts';

test('建档向导 4 步校验（L1 激活链）', () => {
  assert.equal(WIZARD_STEPS.length, 4);
  assert.equal(validateWizardStep(0, { species: '犬' }), null);
  assert.equal(validateWizardStep(0, {}), 'SPECIES_REQUIRED');
  assert.equal(validateWizardStep(1, { species: '犬', name: ' 旺财 ', breed: '柯基' }), null);
  assert.equal(validateWizardStep(1, { name: '', breed: '柯基' }), 'NAME_INVALID');
  assert.equal(validateWizardStep(1, { name: '旺财', breed: '' }), 'BREED_REQUIRED');
  assert.equal(validateWizardStep(2, {}), null); // 照片可跳过
  assert.equal(validateWizardStep(3, { weightKg: 12.5 }), null);
  assert.equal(validateWizardStep(3, { weightKg: 0.05 }), 'WEIGHT_INVALID');
});

test('wizardComplete 需四步全过', () => {
  assert.equal(wizardComplete({ species: '猫', name: '咪咪', breed: '英短', weightKg: 4 }), true);
  assert.equal(wizardComplete({ species: '猫', name: '咪咪', breed: '英短' }), false);
});

test('七类记录字段定义（需求 B4）', () => {
  const kinds = ['疫苗', '驱虫', '体检', '就医', '用药', '过敏', '体重'];
  for (const k of kinds) assert.ok(recordFields(k).length >= 2, k);
  const vaccine = recordFields('疫苗');
  assert.ok(vaccine.some((f) => f.key === 'nextReminderDate')); // B4→B5 联动
  const med = recordFields('用药');
  assert.ok(med.some((f) => f.key === 'validFrom') && med.some((f) => f.key === 'validTo'));
});

test('记录校验：必填/用药起止/未来事件（U67）', () => {
  assert.equal(validateRecord('过敏', {}), 'REQUIRED:kindName');
  const today = new Date().toISOString().slice(0, 10);
  assert.equal(
    validateRecord('用药', { kindName: '药', validFrom: today, validTo: '2020-01-01' }),
    'MED_COURSE_INVALID',
  );
  const tomorrow = new Date(Date.now() + 86400000).toISOString().slice(0, 10);
  assert.equal(
    validateRecord('体检', { eventDate: tomorrow, note: 'x' }),
    'EVENT_FUTURE_NOT_ALLOWED',
  );
  assert.equal(validateRecord('过敏', { kindName: '牛肉', note: '呕吐' }), null);
});

test('周期规则与后端解析格式严格互转（决议格式）', () => {
  assert.equal(buildRecurrenceRule({ kind: 'once', date: '2026-10-01' }), '2026-10-01');
  assert.equal(
    buildRecurrenceRule({ kind: 'everyDays', days: 30, start: '2026-09-01' }),
    '30|2026-09-01',
  );
  assert.equal(
    buildRecurrenceRule({ kind: 'yearly', month: 2, day: 29, start: '2028-03-01' }),
    '2|29|2028-03-01',
  );
  assert.throws(() => buildRecurrenceRule({ kind: 'everyDays', days: 0, start: '2026-09-01' }));
  assert.throws(() => buildRecurrenceRule({ kind: 'yearly', month: 13, day: 1, start: '2026-01-01' }));

  assert.deepEqual(parseRecurrenceRule('everyDays', '30|2026-09-01'), {
    kind: 'everyDays',
    days: 30,
    start: '2026-09-01',
  });
  assert.deepEqual(parseRecurrenceRule('once', '2026-10-01'), { kind: 'once', date: '2026-10-01' });
  assert.deepEqual(parseRecurrenceRule('yearly', '2|29|2028-03-01'), {
    kind: 'yearly',
    month: 2,
    day: 29,
    start: '2028-03-01',
  });
  assert.throws(() => parseRecurrenceRule('bogus', 'x'));
});

test('体重二次确认与趋势高亮（10%/5% 决议）', () => {
  assert.equal(weightSheetConfirm(10, 11), true);
  assert.equal(weightSheetConfirm(10, 10.9), false);
  assert.equal(weightSheetConfirm(null, 50), false);
  assert.equal(trendHighlight(10, 10.5), true);
  assert.equal(trendHighlight(null, 12), false);
});

test('摘要勾选模型（U79：取消/缺项可见）', () => {
  const model = summaryPreview(['档案资料', '提醒计划'], ['档案资料', '健康记录', '提醒计划']);
  assert.deepEqual([...model.included].sort(), ['档案资料', '提醒计划'].sort());
  assert.ok(model.omitted.some((o) => o.module === '健康记录' && o.reason.includes('取消')));
  const missing = summaryPreview(['不存在的模块'], ['档案资料']);
  assert.ok(missing.omitted.some((o) => o.reason.includes('缺项')));
});

test('删除/归档确认文案（U66 软删30天）', () => {
  assert.ok(deleteConfirmCopy('ACTIVE')!.detail.includes('30 天'));
  assert.ok(deleteConfirmCopy('DELETED')!.title.includes('恢复'));
  assert.ok(deleteConfirmCopy('ARCHIVED')!.detail.includes('保留'));
});
