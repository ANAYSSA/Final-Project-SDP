"""Build the concise final PDF. Requires reportlab and Pillow; run Maven verify first."""
from io import BytesIO
from pathlib import Path
import textwrap
import xml.etree.ElementTree as ET
from PIL import Image as PillowImage
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle, Image, Preformatted, KeepTogether
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.utils import ImageReader

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / 'docs'
OUT = DOCS / 'REPORT.pdf'
styles = {
    'body': ParagraphStyle('body', fontName='Times-Roman', fontSize=11, leading=14, spaceAfter=7, textColor=colors.black),
    'title': ParagraphStyle('title', fontName='Times-Bold', fontSize=17, leading=21, spaceAfter=10),
    'h': ParagraphStyle('h', fontName='Times-Bold', fontSize=11, leading=14, spaceBefore=5, spaceAfter=5),
    'small': ParagraphStyle('small', fontName='Times-Roman', fontSize=9, leading=11, spaceAfter=5),
    'cell': ParagraphStyle('cell', fontName='Times-Roman', fontSize=9.7, leading=12),
    'code': ParagraphStyle('code', fontName='Courier', fontSize=7.6, leading=9.1, spaceAfter=4),
}
story = []
def P(text, style='body'): return Paragraph(text, styles[style])
def p(text, style='body'): story.append(P(text, style))
def title(text): p(text, 'title')
def h(text): p(text, 'h')
def page(text): story.append(PageBreak()); title(text)
def table(rows, widths):
    t=Table([[P(str(c),'cell') for c in row] for row in rows], colWidths=widths, hAlign='LEFT')
    t.setStyle(TableStyle([('VALIGN',(0,0),(-1,-1),'TOP'),('LINEABOVE',(0,0),(-1,0),.6,colors.black),('LINEBELOW',(0,0),(-1,0),.6,colors.black),('LINEBELOW',(0,-1),(-1,-1),.6,colors.black),('TOPPADDING',(0,0),(-1,-1),5),('BOTTOMPADDING',(0,0),(-1,-1),5),('LEFTPADDING',(0,0),(-1,-1),5),('RIGHTPADDING',(0,0),(-1,-1),5)]))
    story.append(t);story.append(Spacer(1,7))
def img(path, maxw=490, maxh=260, crop=None):
    source = str(path)
    if path.suffix.lower() in {'.jpg', '.jpeg'}:
        with PillowImage.open(path) as original:
            monochrome = original.convert('L')
            if crop is not None:
                monochrome = monochrome.crop(crop)
            source = BytesIO()
            monochrome.save(source, format='PNG')
            source.seek(0)
    w,h=ImageReader(source).getSize();scale=min(maxw/w,maxh/h)
    return Image(source,w*scale,h*scale,hAlign='CENTER')
def excerpt(label, relative, start, count, breaks=()):
    path=ROOT/'src/main/java/com/restaurant'/relative
    lines=path.read_text().splitlines();idx=next(i for i,line in enumerate(lines) if start in line)
    code=textwrap.dedent('\n'.join(lines[idx:idx+count]))
    # Explicit whitespace-only line breaks keep Java identifiers and strings intact.
    for old, new in breaks:
        assert old in code, f'Excerpt changed: {relative}: {old}'
        code = code.replace(old, new)
    assert all(len(line) <= 104 for line in code.splitlines()), f'Excerpt too wide: {relative}'
    story.append(KeepTogether([P(label,'h'),Preformatted(code,styles['code'])]))

tests=[]
for f in sorted((ROOT/'target/surefire-reports').glob('TEST-*.xml')):
    x=ET.parse(f).getroot();tests.append((int(x.attrib['tests']),int(x.attrib['failures']),int(x.attrib['errors'])))
assert tests and all(f==0 and e==0 for _,f,e in tests), 'Run passing tests first'
count=sum(t for t,_,_ in tests)

title('Restaurant Ordering System')
p('Hearth &amp; Table | Software Design Patterns | 13 November 2026','small')
p('Team: Anuar, Akmaral and Mukhammad','small')
p('GitHub: <link href="https://github.com/ANAYSSA/Final-Project-SDP">https://github.com/ANAYSSA/Final-Project-SDP</link>','small')
p('Website: <link href="https://hearth-table.178.62.244.88.sslip.io">https://hearth-table.178.62.244.88.sslip.io</link>','small')
h('1. Introduction')
p('We developed a restaurant ordering application with Java 21 and Spring Boot. The interface uses HTML, CSS and JavaScript. Customers can choose pizza, pasta or drinks, add toppings, select dine-in or takeaway, apply a discount and place an order. The order page shows status updates and receipts. Prices are in KZT. The project continues the pizzeria examples from our earlier assignments.')
h('2. Team contributions')
table([
 ['<b>Member</b>','<b>Work and Git evidence</b>'],
 ['Anuar (ANAYSSA)','Builder and Factory Method; initial application, web integration and deployment. Baseline: <b>954a3c8</b>.'],
 ['Akmaral (arynttai)','FAMILY discount: <b>1debe3f</b>. JSON receipt renderer: <b>7475264</b>. Unit/API tests. Merged in <link href="https://github.com/ANAYSSA/Final-Project-SDP/pull/1">PR #1</link>.'],
 ['Mukhammad (Vertex-blip)','Herbs decorator: <b>f51cc9c</b>. Operations observer: <b>b2e5b55</b>. Event display and tests. Merged in <link href="https://github.com/ANAYSSA/Final-Project-SDP/pull/2">PR #2</link>.']
],[128,367])
p('Both pull requests were merged without squashing. The original authors and implementation commits remain in the repository.','small')
h('3. Patterns used')
table([
 ['<b>Pattern / type</b>','<b>Use in the application</b>'],
 ['Builder / creational','Order.Builder assembles and validates an immutable order. Dine-in requires a table; takeaway must not include one.'],
 ['Factory Method / creational','MealFactory delegates creation to PizzaFactory, PastaFactory or DrinkFactory. Checkout works with the Meal interface.'],
 ['Decorator / structural','Toppings wrap a Meal. Herbs adds 150 KZT per unit and can combine with other toppings. Drinks reject toppings.'],
 ['Strategy / behavioral','PricingCalculator uses one DiscountStrategy. FAMILY gives 15% off a decorated subtotal of at least 6000 KZT. Lower subtotals are rejected.'],
 ['Bridge / structural','CustomerReceipt and KitchenTicket use the same renderer contract. Each supports text, HTML or JSON without separate classes for every combination.'],
 ['Observer / behavioral','OrderProgress notifies customer, kitchen and operations subscribers. Operations messages contain only a short reference, status and item quantity.']
],[138,357])
p('The source has six pattern packages with role subfolders. Main is the only class at the package root. HTTP adapters are separate from the domain classes. Quote and order creation share the same calculation, and BigDecimal handles money with scale 2 and HALF_UP rounding.','small')

page('4. UML class diagrams')
p('These diagrams show the main roles. Full diagrams and editable PlantUML sources are included in docs/uml. Some concrete variants are omitted here to keep the diagrams readable.','small')
rows=[]
for pair in [('builder','factory'),('decorator','strategy'),('bridge','observer')]:
    row=[]
    for name in pair:
        row.append([P(name.replace('factory','Factory Method').title(),'h'),img(DOCS/'uml'/f'report-{name}.png',237,166)])
    rows.append(row)
t=Table(rows,colWidths=[247.5,247.5],hAlign='LEFT')
t.setStyle(TableStyle([('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),0),('RIGHTPADDING',(0,0),(-1,-1),10),('BOTTOMPADDING',(0,0),(-1,-1),17)]))
story.append(t)
p('Factory creates the meal; decorators add extras; Builder creates the order; Strategy calculates its price; Observer publishes progress; Bridge produces the selected receipt.','small')

page('5. Code examples and verification')
p('Selected excerpts from the Java implementation; complete classes and tests are in the repository.','small')
excerpt('Builder - OrderService.java','builder/application/OrderService.java','Order.Builder builder =',4, [
    ('new Order.Builder().customer', 'new Order.Builder()\n    .customer'),
    ('.discount(discounts.find', '\n    .discount(discounts.find'),
])
excerpt('Factory Method - MealFactory.java','factory/creator/MealFactory.java','public final Meal create',6, [
    (') throw new', ')\n        throw new'),
])
excerpt('Decorator - HerbsDecorator.java','decorator/concrete/HerbsDecorator.java','public final class HerbsDecorator',3)
excerpt('Strategy - FamilyDiscount.java','strategy/concrete/FamilyDiscount.java','public BigDecimal discount',6, [
    ('throw new IllegalArgumentException("Family', 'throw new IllegalArgumentException(\n            "Family'),
])
excerpt('Bridge - JsonReceiptRenderer.java','bridge/implementation/JsonReceiptRenderer.java','public String render',10)
excerpt('Observer - OperationsDashboardObserver.java','observer/concrete/OperationsDashboardObserver.java','@Override public OrderNotification',5)
h('Verification')
p(f'The combined project passed <b>{count} tests with no failures or errors</b> on Java 21. Tests cover discount boundaries, topping composition, JSON/HTML escaping, status transitions, concurrent retries and access from another session. The console --demo and package-structure check also pass.','small')
p('Example checked in the browser: three Margheritas with herbs cost 6450 KZT. FAMILY subtracts 967.50 KZT, giving a total of 5482.50 KZT. Advancing the status adds customer, kitchen and operations events.','small')
h('Conclusion and further work')
p('All six patterns work in one ordering flow. Narrow interfaces keep pricing, rendering and notification responsibilities separate; shared calculations avoid duplicating pricing rules. Orders currently live in the browser session and disappear after expiry or server restart. The next steps would be a database and staff authentication, followed by a kitchen screen to replace the simulated status button.','small')

page('6. Application screenshots')
story.append(img(DOCS/'screenshots/menu-final.jpg',495,298))
p('Figure 1. The deployed menu and checkout with the merged Herbs topping and FAMILY discount.','small')
story.append(Spacer(1,7))
story.append(img(DOCS/'screenshots/orders-final.jpg',495,298,crop=(125,405,1585,1205)))
p('Figure 2. Order progress, session-scoped notifications and receipt links in text, HTML and JSON.','small')
p('The application runs on DigitalOcean behind an HTTPS reverse proxy. This is a course demonstration: it does not collect payments or send orders to a real restaurant. The status button simulates preparation.','small')

def footer(c,doc):
    c.setFont('Times-Roman',9);c.drawCentredString(297.64,27,str(doc.page))

SimpleDocTemplate(str(OUT),pagesize=(595.28,841.89),leftMargin=50,rightMargin=50,topMargin=42,bottomMargin=44,title='Restaurant Ordering System - Final Report',author='Anuar, Akmaral, Mukhammad').build(story,onFirstPage=footer,onLaterPages=footer)
print(OUT)
