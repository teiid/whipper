import { Scenario } from './scenario';
import { JobInfo } from './job-info';

export class Job {
  id: string;
  job_info: JobInfo;
  scenarios: Scenario[];
}
