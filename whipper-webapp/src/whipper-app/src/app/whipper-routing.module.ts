import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { WelcomeComponent } from './welcome.component';
import { JobsComponent } from './jobs.component';
import { JobDetailComponent } from './job-detail.component';
import { ResultDetailComponent } from './result-detail.component';
import { StartJobComponent } from './start-job.component';
import { QuerySetsComponent } from './query-sets.component';
import { QuerySetDetailComponent } from './query-set-detail.component';
import { SuiteDetailComponent } from './suite-detail.component';
import { ExpectedResultDetailComponent } from './expected-result-detail.component';
import { ScenariosComponent } from './scenarios.component';
import { ScenarioDetailComponent } from './scenario-detail.component';

const routes: Routes = [
  {path: '', component: WelcomeComponent},
  {path: 'jobs', component: JobsComponent},
  {path: 'jobs/:id', component: JobDetailComponent},
  {path: 'jobs/:id/results', component: ResultDetailComponent},
  {path: 'start-job', component: StartJobComponent},
  {path: 'query-sets', component: QuerySetsComponent},
  {path: 'query-sets/:id', component: QuerySetDetailComponent},
  {path: 'query-sets/:id/suite/:suiteid', component: SuiteDetailComponent},
  {path: 'query-sets/:id/exp-res/:expresid', component: ExpectedResultDetailComponent},
  {path: 'scenarios', component: ScenariosComponent},
  {path: 'scenarios/:id', component: ScenarioDetailComponent}
]

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})

export class WhipperRoutingModule{}
