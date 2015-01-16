import core.board.ClientBoard
import core.board.ClientCell
import core.board.ClientSpell
import shared.board.Board
import shared.board.Cell
import shared.board.Spell
import shared.board.Unit
import core.graphics.scenes.BattleScene
import core.graphics.scenes.Scenes
import program.main.SceneUtil

def onCheck(Spell spell, Board board, Cell target){
    target.contentsType == Cell.CONTENTS_UNIT && target.unit.state != Unit.STATE_DEAD
}

def onCheckAOE(Spell spell, Board board, Cell from, Cell to){
    from == to
}

def onCastBegin(ClientSpell spell, Board board, ClientCell target){
	spell.caster.setFacing(target.unit);
	1.0F
}

def onCastEnd(Spell spell, ClientBoard board, Cell target){
    def from = SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(spell.caster).node,
        to = SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(target.unit).node;

    board.addBuff("poisonBuff", 4, 1, target.unit)
	board.nextTurn()
	spell.putOnCoolDown()

    board.addEffect("fireball-effect", from, to)
}