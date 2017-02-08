import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { WelcomeComponent } from './welcome.component';
import { JobsComponent } from './jobs.component';
import { JobDetailComponent } from './job-detail.component';
import { ResultsComponent } from './results.component';
import { ResultDetailComponent } from './result-detail.component';

const routes: Routes = [
  {path: '', component: WelcomeComponent},
  {path: 'jobs', component: JobsComponent},
  {path: 'jobs/:id', component: JobDetailComponent},
  {path: 'results', component: ResultsComponent},
  {path: 'results/:id', component: ResultDetailComponent}
]

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})

export class WhipperRoutingModule{}