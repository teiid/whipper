import { WhipperAppPage } from './app.po';

describe('whipper-app App', function() {
  let page: WhipperAppPage;

  beforeEach(() => {
    page = new WhipperAppPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
