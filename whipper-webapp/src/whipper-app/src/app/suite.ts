import { QuerySet } from './query-set';

export class Suite {
  id: string;
  all: number;
  fail: number;
  pass: number;
  skip: number;
  querySets: QuerySet[];
}
