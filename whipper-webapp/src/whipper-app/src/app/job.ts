import { Scenario } from './scenario';
import { JobInfo } from './job-info';

export class Job {
  id: string;
  job-info: JobInfo;
  scenarios: Scenario[];
}
