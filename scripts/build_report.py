"""Build the English defense report after tests and browser screenshots exist.
Optional tooling: pip install reportlab. Run from any working directory.
"""
from pathlib import Path
from xml.sax.saxutils import escape
import xml.etree.ElementTree as ET
from reportlab.pdfgen import canvas
from reportlab.platypus import BaseDocTemplate, Frame, PageTemplate, NextPageTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle, Image, Preformatted, KeepTogether
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / 'docs'
OUT = DOCS / 'REPORT.pdf'
FONT_DIR = Path('/System/Library/Fonts/Supplemental')
if FONT_DIR.exists():
    for name, file in [('Body','Arial.ttf'),('BodyBold','Arial Bold.ttf'),('Display','Georgia.ttf'),('Code','Courier New.ttf')]:
        pdfmetrics.registerFont(TTFont(name, str(FONT_DIR / file)))
else:
    for name, file in [('Body','DejaVuSans.ttf'),('BodyBold','DejaVuSans-Bold.ttf'),('Display','DejaVuSerif.ttf'),('Code','DejaVuSansMono.ttf')]:
        found = next(Path('/usr/share/fonts').rglob(file))
        pdfmetrics.registerFont(TTFont(name, str(found)))
pdfmetrics.registerFontFamily('Body', normal='Body', bold='BodyBold', italic='Body', boldItalic='BodyBold')
INK=colors.HexColor('#252824'); ORANGE=colors.HexColor('#AE4A2B'); MUTED=colors.HexColor('#64685F'); CREAM=colors.HexColor('#F7F4EC'); LINE=colors.HexColor('#DEDCD1')
styles=getSampleStyleSheet()
styles.add(ParagraphStyle(name='BodyText2',fontName='Body',fontSize=10.2,leading=15,textColor=INK,spaceAfter=9))
styles.add(ParagraphStyle(name='Title2',fontName='Display',fontSize=30,leading=36,textColor=INK,spaceAfter=20))
styles.add(ParagraphStyle(name='H1a',fontName='Display',fontSize=23,leading=29,textColor=INK,spaceAfter=16))
styles.add(ParagraphStyle(name='H2a',fontName='BodyBold',fontSize=12,leading=17,textColor=ORANGE,spaceBefore=12,spaceAfter=8))
styles.add(ParagraphStyle(name='Label',fontName='BodyBold',fontSize=9,leading=13,textColor=ORANGE,spaceAfter=12))
styles.add(ParagraphStyle(name='Small2',fontName='Body',fontSize=8.3,leading=12,textColor=MUTED,spaceAfter=8))
styles.add(ParagraphStyle(name='Code2',fontName='Code',fontSize=7.5,leading=10,textColor=INK,backColor=CREAM,borderPadding=8,spaceAfter=10))
story=[]
def p(text, style='BodyText2'): story.append(Paragraph(text,styles[style]))
def h(text): p(text,'H2a')
def page(label,title):
    if story: story.append(PageBreak())
    p(label.upper(),'Label'); p(title,'H1a')
def table(rows,widths=None):
    data=[[Paragraph(escape(str(c)),styles['Small2']) for c in row] for row in rows]
    t=Table(data,colWidths=widths,hAlign='LEFT',repeatRows=1)
    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),CREAM),('LINEBELOW',(0,0),(-1,0),1,ORANGE),('LINEBELOW',(0,1),(-1,-1),.3,LINE),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),6)]))
    story.append(t); story.append(Spacer(1,10))
def figure(path,maxh=270,maxw=475):
    w,h=ImageReader(str(path)).getSize(); factor=min(maxw/w,maxh/h)
    story.append(Image(str(path),w*factor,h*factor,hAlign='CENTER'));story.append(Spacer(1,8))
def code(rel,start=None,end=None):
    lines=(ROOT/rel).read_text().splitlines()
    if start:
        idx=next(i for i,s in enumerate(lines) if start in s); lines=lines[idx:]
    if end: lines=lines[:end]
    # Common leading whitespace can be removed without changing code content.
    import textwrap
    text=textwrap.dedent('\n'.join(lines))
    text='\n'.join('\n'.join(textwrap.wrap(line, width=100, subsequent_indent='    ', replace_whitespace=False, drop_whitespace=False)) if len(line)>100 else line for line in text.splitlines())
    story.append(Preformatted(text,styles['Code2']))
    p(rel,'Small2')

def footer(c,doc):
    c.setStrokeColor(LINE);c.line(55,46,c._pagesize[0]-55,46)
    c.setFont('Body',8);c.setFillColor(MUTED);c.drawString(55,31,'HEARTH & TABLE  /  SOFTWARE DESIGN PATTERNS')
    c.drawRightString(c._pagesize[0]-55,31,str(doc.page))

p('SOFTWARE DESIGN PATTERNS / FINAL PROJECT','Label')
story.append(Spacer(1,50))
p('Hearth &amp; Table','Title2')
p('Restaurant Ordering System','H1a')
p('A coordinated Java application with six design patterns, a browser-based ordering experience, and a reproducible deployment.')
story.append(Spacer(1,22))
table([['TEAM MEMBER','ASSIGNED RESPONSIBILITY'],['Anuar','Builder + Factory Method'],['Akmaral','Strategy + Bridge'],['Mukhammad','Observer + Decorator']],[145,330])
story.append(Spacer(1,20))
p('Java 21  /  Spring Boot 3.5.16  /  HTML, CSS &amp; JavaScript','Label')
p('Report edition: 13 November 2026. Integrated baseline; contribution verification remains open until the other two members complete and merge their implementations.','Small2')
p('Repository: <link href="https://github.com/ANAYSSA/Final-Project-SDP" color="#AE4A2B">github.com/ANAYSSA/Final-Project-SDP</link>','Small2')
p('Live demonstration: <link href="https://hearth-table.178.62.244.88.sslip.io" color="#AE4A2B">hearth-table.178.62.244.88.sslip.io</link>','Small2')

page('01 / Introduction','From separate exercises to one system')
p('A restaurant order combines several independent decisions: what meal to create, which extras to add, how to assemble customer details, which pricing rule to apply, how to present a receipt and who should react to progress. Mixing these decisions in one controller makes changes difficult to explain and test.')
p('Hearth &amp; Table separates those decisions into six explicit design patterns. The customer can browse pizza, pasta and drinks; choose toppings; order for a table or takeaway; see a server-calculated quote; submit an order; and inspect status notifications and two kinds of receipt.')
h('Project scope')
p('This is an educational ordering demonstration. No payment is collected and no real kitchen, email or SMS service receives an order. A clearly labelled control advances the simulated kitchen status. The Observer mechanism executes in Java; the browser displays the resulting events.')
h('Core requirements and evidence')
table([['REQUIREMENT','EVIDENCE'],['Six distinct patterns','Two creational, two structural and two behavioral patterns in the same flow.'],['Clean architecture','Six pattern packages with role-specific subfolders; Main is the sole root Java source.'],['Code + report','Runnable Java sources, tests, this PDF, UML sources/figures and real application screenshots.'],['Individual implementation','Assigned pairs are documented; Akmaral and Mukhammad must still implement and commit their contributions.'],['Submission','Every student submits a PDF report, source ZIP and GitHub link before the lecturer deadline. The supplied material did not state a date.']],[140,335])
p('The stated grading weights are 50% code and Git link, 10% report, and 40% defense. The additional requirements specify a 5% deduction per violation. The checklist in docs/REQUIREMENTS.md is the submission control document.','Small2')

page('02 / Architecture','One application, six pattern packages')
p('Main is the composition root and supports both Spring Boot startup and an integrated --demo console run. The builder package contains the order product and the application/HTTP boundary. All other domain packages express named pattern roles; static web assets live under src/main/resources/static.')
table([['PACKAGE','ROLE IN THE ORDER FLOW'],['factory','Menu definitions enter concrete creators and produce Meal implementations.'],['decorator','Selected extras wrap a Meal while preserving its component interface.'],['builder','Validated customer details and order lines become an immutable Order.'],['strategy','A selected discount algorithm computes the final BigDecimal price.'],['bridge','Receipt purpose and output format evolve independently.'],['observer','Status changes notify customer and kitchen subscribers.']],[105,370])
h('Coordinated execution')
p('Factory Method creates a Meal; Decorator adds extras; Builder validates the order; Strategy prices it; Observer publishes progress; Bridge renders purpose-specific receipts. The landscape appendix shows the actual call sequence.')
p('The browser sends identifiers and quantities, never authoritative prices. The service re-creates each meal and recalculates its decorated price. Receipt and history endpoints use the same session ownership checks.','Small2')

patterns=[
('03','Builder','Creational / Anuar','builder',
 'Order.Builder accumulates customer, fulfillment, table, note, order lines and the chosen discount. build() validates the complete configuration before creating an immutable Order through a private constructor. The built product copies its item list, so later builder use cannot mutate an existing order.',
 'A dine-in order requires a table from 1 to 30. Takeaway rejects a table. Blank names, empty orders and invalid quantities are rejected. Fluent construction keeps the call readable as optional details grow.',
 'src/main/java/com/restaurant/builder/product/Order.java','public static final class Builder',17),
('04','Factory Method','Creational / Anuar','factory',
 'MealFactory is the abstract creator. Its public create operation validates the menu category and delegates construction to the protected factory method createMeal. PizzaFactory, PastaFactory and DrinkFactory override that method to create concrete Meal products.',
 'MenuCatalog chooses a creator for the requested category. This registration step is not itself counted as the pattern: the abstraction and overriding creation operation are the Factory Method roles.',
 'src/main/java/com/restaurant/factory/creator/MealFactory.java','public abstract class MealFactory',12),
('05','Strategy','Behavioral / Akmaral','strategy',
 'PricingCalculator is the context and depends on DiscountStrategy. NoDiscount, StudentDiscount and PickupDiscount implement interchangeable rules. Student discount is 10%; pickup discount is 20% and requires takeaway. Only one selected rule applies.',
 'The decorated subtotal is calculated before discount. For two Margheritas with cheese: (2000 + 400) x 2 = 4800 KZT. Student discount is 480 KZT and the total is 4320 KZT. BigDecimal uses two decimal places and HALF_UP rounding.',
 'src/main/java/com/restaurant/strategy/context/PricingCalculator.java','public final class PricingCalculator',12),
('06','Bridge','Structural / Akmaral','bridge',
 'ReceiptDocument is the abstraction, refined by CustomerReceipt and KitchenTicket. ReceiptRenderer is the independent implementation hierarchy, with TextReceiptRenderer and HtmlReceiptRenderer. Each purpose can use either format through composition.',
 'Customer receipts include prices and totals; kitchen tickets focus on preparation details. HTML output escapes dynamic content. A new renderer can serve both receipt types without creating a class for every purpose-format combination.',
 'src/main/java/com/restaurant/bridge/abstraction/ReceiptDocument.java',None,30),
('07','Observer','Behavioral / Mukhammad','observer',
 'OrderProgress is the subject. OrderObserver is the subscriber contract, implemented by CustomerObserver and KitchenObserver. It publishes the initial placement and valid changes; each subscriber produces an audience-specific notification.',
 'The lifecycle is PLACED, PREPARING, READY, COMPLETED. Advancing a completed order is rejected. Subscription and unsubscription are explicit. Browser refresh is a display mechanism; notification dispatch is the backend Observer pattern.',
 'src/main/java/com/restaurant/observer/api/OrderObserver.java',None,20),
('08','Decorator','Structural / Mukhammad','decorator',
 'Meal is the component shared by base products and wrappers. ToppingDecorator holds a wrapped Meal. ExtraCheese, Mushrooms and Olives add price and topping descriptions without modifying Pizza or Pasta classes.',
 'Wrappers can compose in either order and preserve identity and category. Cheese adds 400 KZT, mushrooms 300 KZT and olives 250 KZT per unit. Drinks cannot receive toppings. The server applies each selected topping once and rejects duplicate IDs.',
 'src/main/java/com/restaurant/decorator/component/ToppingDecorator.java',None,32)
]
for num,name,owner,diagram,explain,scenario,path,start,end in patterns:
    page(num+' / '+owner,name)
    p(explain);p(scenario)
    figure(DOCS/'uml'/f'{diagram}.png',215)
    h('Java implementation excerpt')
    code(path,start,end)

page('09 / User interface','Menu and order composition')
p('The menu combines category filters, search and item customization. Prices are in KZT. Toppings and discounts come from the backend catalog, so new registered implementations can appear without hard-coding their prices in the browser.')
figure(DOCS/'screenshots/menu.jpg',330)
p('Figure 1. Real browser capture of the running application. The cart remains visible on wide screens; the layout adapts for narrower devices.','Small2')
h('Checkout behavior')
p('The browser requests a quote whenever the cart or pricing choices change. Older responses are ignored when a newer quote is pending. Order creation uses a UUID request identifier to make retries idempotent. Validation errors remain visible near the relevant workflow.')
p('Customer details are used only by this demonstration. The API stores order history in the current HTTP session. The UI explains that a server restart or expired session clears this history.')

page('10 / User interface','Progress, notifications and receipts')
figure(DOCS/'screenshots/orders.jpg',340)
p('Figure 2. A placed order with its lifecycle and Java observer notifications. The demo status control makes simulation explicit.','Small2')
h('An integrated demonstration')
p('Create a Margherita with cheese, select a quantity of two and the student discount, then place the order. Inspect the total of 4320 KZT, advance the order status and show the new customer/kitchen events. Open both receipt types in text and HTML. This one sequence exercises all six patterns.')
h('Independent verification')
p('A second browser session cannot access the first session\'s order ID. Unsupported receipt formats, unknown menu IDs, toppings on drinks, invalid discounts and terminal state transitions return useful errors rather than arbitrary results.')

page('11 / Engineering quality','Clean code backed by executable checks')
table([['PRINCIPLE','CONCRETE APPLICATION'],['Single responsibility','Factories create products; decorators add extras; pricing calculates totals; renderers format output.'],['Open/closed','New discount, topping, renderer and observer implementations extend existing contracts; registration exposes them.'],['Liskov substitution','Products and decorators satisfy Meal; each renderer serves both receipt abstractions.'],['Interface segregation','Meal, DiscountStrategy, ReceiptRenderer and OrderObserver expose narrow domain operations.'],['Dependency inversion','Pricing, receipt abstractions and the subject depend on interfaces rather than concrete variants.'],['KISS / DRY','One server calculation drives quote and placement; catalogs centralize prices; no database or payment integration is added without a requirement.']],[120,355])
h('Automated verification')
results=[]
for f in (ROOT/'target/surefire-reports').glob('TEST-*.xml'):
    x=ET.parse(f).getroot(); results.append((x.attrib['name'].split('.')[-1],int(x.attrib['tests']),int(x.attrib['failures'])+int(x.attrib['errors'])))
if not results: raise RuntimeError('Run Maven verification before building the report')
table([['TEST SUITE','TESTS','FAILURES / ERRORS']]+[[a,b,c] for a,b,c in results],[285,70,120])
p('Verification commands: ./mvnw verify; python3 scripts/check_structure.py; java -jar target/hearth-and-table-1.0.0.jar --demo. GitHub Actions repeats these checks on push and pull request.','Small2')
p('Tests target domain invariants and integration behavior: creation, wrapping, money, invalid input, request retries, session isolation, status transitions and escaped receipt content. Browser checks validate the customer workflow and responsive layout; these are separate from the Java test suite.')

page('12 / Deployment and conclusion','A reproducible teaching application')
p('The application is deployed to the existing DigitalOcean Droplet. A Java 21 container runs the tested JAR; a separate Caddy container terminates HTTPS and proxies requests. The deployment uses a dedicated Compose project and caps memory consumption. No additional paid server is required.')
h('Operational boundaries')
p('This is a single-instance, session-based demonstration. Data is not durable across restarts, and the server shares resources with another service. Each session is bounded to 20 orders. There is no account system, real kitchen terminal, payment processor or external notification transport. These are deliberate limits of the implemented scope.')
h('Conclusion')
p('The result integrates six distinct patterns into a concrete customer journey. Creation, optional behavior, configuration, pricing, output and notifications have separate extension points. The same domain works through the browser and through the Main console demonstration, making pattern behavior inspectable during defense.')
h('Further work')
p('The next team iteration adds Akmaral\'s FAMILY strategy and JSON renderer, plus Mukhammad\'s herbs decorator and operations observer. Subsequent product work could add a durable database, staff authentication, audited kitchen controls, inventory availability and a payment-provider sandbox. Each addition should preserve the current domain contracts and session-ownership guarantees.')
h('Before submitting')
p('Complete both teammates\' individual implementation work, preserve their original Git authors through PR merges, rerun the combined checks, update this report\'s contribution evidence and regenerate the source ZIP. Every student must submit personally by the lecturer\'s deadline. The repository alone does not satisfy individual implementation or submission requirements.')

page('13 / Sources and evidence','Project lineage and contribution record')
p('The earlier assignments established a pizzeria domain and isolated pattern examples. The final project reuses the domain vocabulary and familiar pricing ideas while adding packages, a web API, validation, tests and an integrated customer flow.')
refs=[
('Assignment 1 - Builder','https://github.com/ANAYSSA/SDPAssignment1Builder'),
('Assignment 2 - Abstract Factory','https://github.com/ANAYSSA/Assignment2SDP_AbstractFactory_AnuarM'),
('Assignment 3 - Bridge','https://github.com/ANAYSSA/sdp-assignment-3'),
('Assignment 4 - Facade','https://github.com/ANAYSSA/as4-facade-pizzeria'),
('Assignment 5 - Strategy','https://github.com/ANAYSSA/as5-strategy-pizzeria'),
('Assignment 6 - Visitor','https://github.com/ANAYSSA/as6-visitor-pizzeria'),
('Spring Boot system requirements','https://docs.spring.io/spring-boot/3.5/system-requirements.html'),
('DigitalOcean Droplets documentation','https://docs.digitalocean.com/products/droplets/'),
('Caddy automatic HTTPS','https://caddyserver.com/docs/automatic-https')]
for title,url in refs:
    p(f'<b>{escape(title)}</b><br/><link href="{url}" color="#AE4A2B">{escape(url)}</link>','Small2')
h('Authorship is evidenced by Git, not this table')
table([['MEMBER','BASELINE CONTRIBUTION STATUS'],['Anuar','Integrated baseline prepared and committed using the authenticated ANAYSSA identity; assigned Builder and Factory Method.'],['Akmaral','Assigned Strategy and Bridge. Individual extension commits and personal explanation pending.'],['Mukhammad','Assigned Observer and Decorator. Individual extension commits and personal explanation pending.']],[110,365])
p('Inspect git log and actual diffs after merging the teammate PRs. docs/TEAM_TASKS.md contains their scoped implementation prompts and acceptance checks. Do not replace those contributions with fabricated author records. AI assistance does not remove the obligation to understand and defend the code.','Small2')
p('Requirement sources: Final Project Defense Requirements.pdf supplied by the team, and the additional requirements supplied with the project request. No submission deadline was stated in those materials.','Small2')

story.append(NextPageTemplate('landscape'))
page('14 / UML appendix','Integrated order sequence')
figure(DOCS/'uml/integrated-sequence.png',390,730)
p('Selected calls from the implemented application. The full editable PlantUML source and SVG export are in docs/uml.','Small2')
doc=BaseDocTemplate(str(OUT),pagesize=(595.28,841.89),title='Hearth & Table - Restaurant Ordering System',author='Anuar, Akmaral, Mukhammad')
doc.addPageTemplates([PageTemplate(id='portrait',pagesize=(595.28,841.89),frames=[Frame(55,62,485.28,727.89,leftPadding=0,rightPadding=0,topPadding=0,bottomPadding=0)],onPage=footer), PageTemplate(id='landscape',pagesize=(841.89,595.28),frames=[Frame(55,60,731.89,483.28,leftPadding=0,rightPadding=0,topPadding=0,bottomPadding=0)],onPage=footer)])
doc.build(story)
print(OUT)
