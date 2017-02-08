import { Suite } from './suite';

export class Scenario {
  id: string;
  all: number;
  fail: number;
  pass: number;
  skip: number;
  suites: Suite[];
}
