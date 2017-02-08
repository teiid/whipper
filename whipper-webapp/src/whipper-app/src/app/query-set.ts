import { Query } from './query';

export class QuerySet {
  id: string;
  all: number;
  fail: number;
  pass: number;
  skip: number;
  queries: Query[];
}
